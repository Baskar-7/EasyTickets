import Controller from '@ember/controller';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';
import { set } from '@ember/object';
import { inject as service } from '@ember/service';

export default class ManageAccountsController extends Controller {
  @tracked isloading = 'false';
  @tracked tableLayout = {};
  @tracked layout = {};
  @service AjaxUtil;

  updateStatusMessage(jsonData) {
    var statusMessage = {
      status: jsonData.status,
      message: jsonData.message,
      show: 'true',
    };
    set(this, 'statusMessage', statusMessage);
  }

  getLayoutDetails() {
    this.isloading = 'true';
    this.AjaxUtil.ajax('api/json/action/getManageAccLayoutDetails', '').then(
      (jsonData) => {
        this.isloading = 'false';
        var acc_layout = {
          cols: jsonData.acc_cols,
          sortArray: jsonData.sortCol,
          sortingOrder: true,
          filterList: jsonData.acc_filterBy,
          searchBar: true,
          search: '',
          filterValue: 'All',
          actions: ['upgrade', 'delete', 'degrade'],
          actionsFor: {
            upgrade: { 'Acc Type': 'User' },
            degrade: { 'Acc Type': 'Host' },
          },
          label: 'accounts',
        };

        var req_layout = {
          cols: jsonData.req_cols,
          filterList: jsonData.req_filterBy,
          search: '',
          searchBar: true,
          filterValue: 'All',
          actions: ['confirm'],
          label: 'requests',
        };
        this.set('layout', { acc_layout: acc_layout, req_layout: req_layout });
        this.getAccountDetails();
        this.getRequestDetails();
      }
    );
  }

  getAccountDetails() {
    var layout = this.layout.acc_layout;
    var params = {
      search: layout.search,
      sortCol: layout.sortCol || 'fname',
      sortBy: layout.sortingOrder,
      filterBy: layout.filterValue,
    };

    this.isloading = 'true';
    this.AjaxUtil.ajax('api/json/action/getAccountsDetails', {
      params: JSON.stringify(params),
    }).then((jsonData) => {
      this.isloading = 'false';
      this.tableLayout.acc_details = jsonData.accountDetails;
      this.set('tableLayout', this.tableLayout);
    });
  }

  getRequestDetails() {
    var layout = this.layout.req_layout;
    var params = {
      search: layout.search,
      sortCol: layout.sortCol || 'fname',
      sortBy: layout.sortingOrder,
      filterBy: layout.filterValue,
    };

    this.isloading = 'true';
    this.AjaxUtil.ajax('api/json/action/getReqDetails', {
      params: JSON.stringify(params),
    }).then((jsonData) => {
      this.isloading = 'false';
      this.tableLayout.req_details = jsonData.req_details;
      this.set('tableLayout', this.tableLayout);
    });
  }

  @action
  handleRequestActions(actionName, params) {
    console.log(params);
    if (actionName == 'confirm') {
      if (params['Req Type'] == 'Host') {
        Swal.fire({
          title: 'Are you Sure?',
          text: 'Accepting this will make this to Host account!',
          icon: 'warning',
          showCancelButton: true,
          confirmButtonColor: '#3085d6',
          cancelButtonColor: '#d33',
          confirmButtonText: 'Accept',
        }).then((result) => {
          if (result.isConfirmed) {
            this.isloading = 'true';
            this.AjaxUtil.ajax('api/json/action/upgradeAccount', {
              requestId: params.RequestId,
            }).then((jsonData) => {
              this.isloading = 'false';
              this.updateStatusMessage(jsonData);
              if (jsonData.status == 'success') this.getRequestDetails();
              this.getAccountDetails();
            });
          }
        });
      } else {
        Swal.fire({
          title: 'Are you Sure?',
          text: 'This theatre will associate them with their account.',
          icon: 'warning',
          showCancelButton: true,
          confirmButtonColor: '#3085d6',
          cancelButtonColor: '#d33',
          confirmButtonText: 'Accept',
        }).then((result) => {
          if (result.isConfirmed) {
            this.isloading = 'true';
            this.AjaxUtil.ajax('api/json/action/toggleTheatreRequest', {
              mail: params.Mail,
              name: params.Name,
              theatre_id: params.theatre_id,
              status: 'approved',
            }).then((jsonData) => {
              this.isloading = 'false';
              jsonData.message = 'Update successfully!..';
              if (jsonData.status == 'error') {
                jsonData.message = 'Error Occurred while agree the Request!!';
              }
              this.updateStatusMessage(jsonData);
              if (jsonData.status == 'success') this.getRequestDetails();
              this.getAccountDetails();
            });
          }
        });
      }
    } else if (actionName == 'cancel') {
      Swal.fire({
        title: 'Deny Request?',
        input: 'textarea',
        inputAttributes: {
          placeholder: 'Reason.......',
          required: true,
        },
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#3085d6',
        cancelButtonColor: '#d33',
        confirmButtonText: 'Deny',
      }).then((result) => {
        if (result.isConfirmed) {
          if (params['Req Type'] == 'Host') {
            this.isloading = 'true';
            this.AjaxUtil.ajax('api/json/action/denyHostRequest', {
              mail: params.Mail,
              name: params.Name,
              requestId: params.RequestId,
            }).then((jsonData) => {
              this.isloading = 'false';
              this.updateStatusMessage(jsonData);
              if (jsonData.status == 'success') this.getRequestDetails();
            });
          } else {
            this.isloading = 'true';
            this.AjaxUtil.ajax('api/json/action/toggleTheatreRequest', {
              mail: params.Mail,
              name: params.Name,
              theatre_id: params.theatre_id,
              status: 'denied',
              reason: result.value,
              theatreName: params.TheatreName,
            }).then((jsonData) => {
              jsonData.message = 'Request Denied successfully!..';
              this.isloading = 'false';
              if (jsonData.status == 'error') {
                jsonData.message = 'Error Occurred while deny the Request!!';
              }
              this.updateStatusMessage(jsonData);
              if (jsonData.status == 'success') this.getRequestDetails();
            });
          }
        }
      });
    } else if (actionName == 'refresh') {
      this.getRequestDetails();
    }
  }

  @action
  handleAccountActions(actionName, params) {
    if (actionName == 'delete') {
      Swal.fire({
        title: 'Are you sure want to Delete ?',
        text: "You won't be able to revert this!",
        input: 'textarea',
        inputAttributes: {
          placeholder: 'Reason........',
          required: true,
        },
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#3085d6',
        cancelButtonColor: '#d33',
        confirmButtonText: 'Yes, delete it!',
      }).then((result) => {
        if (result.isConfirmed) {
          this.isloading = 'true';
          this.AjaxUtil.ajax('api/json/action/deleteAccount', {
            user_id: params.userId,
            reason: result.value,
          }).then((jsonData) => {
            this.isloading = 'false';
            this.updateStatusMessage(jsonData);
            if (jsonData.status == 'success') this.getAccountDetails();
          });
        }
      });
    } else if (actionName == 'upgrade') {
      Swal.fire({
        title: 'Are you Sure?',
        text: 'This will change the account to a Host account!',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#3085d6',
        cancelButtonColor: '#d33',
        confirmButtonText: 'Confirm',
      }).then((result) => {
        if (result.isConfirmed) {
          this.isloading = 'true';
          this.AjaxUtil.ajax('api/json/action/toggleAccount', {
            user_id: params.userId,
            acc_type: 'Host',
          }).then((jsonData) => {
            this.isloading = 'false';
            this.updateStatusMessage(jsonData);
            if (jsonData.status == 'success') this.getAccountDetails();
          });
        }
      });
    } else if (actionName == 'degrade') {
      Swal.fire({
        title: 'Are you Sure?',
        text: 'Reduce the permissions to the "User" level',
        input: 'textarea',
        inputAttributes: {
          placeholder:
            'Explain the reason for the account privilege adjustment.',
          required: true,
        },
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#3085d6',
        cancelButtonColor: '#d33',
        confirmButtonText: 'Yes',
        cancelButtonText: 'No',
      }).then((result) => {
        if (result.isConfirmed) {
          this.isloading = 'true';
          this.AjaxUtil.ajax('api/json/action/toggleAccount', {
            user_id: params.userId,
            acc_type: 'User',
            reason: result.value,
          }).then((jsonData) => {
            this.isloading = 'false';
            this.updateStatusMessage(jsonData);
            if (jsonData.status == 'success') this.getAccountDetails();
          });
        }
      });
    } else if (actionName == 'refresh') {
      this.getAccountDetails();
    }
  }
}
