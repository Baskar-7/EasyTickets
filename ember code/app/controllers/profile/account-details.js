import Controller from '@ember/controller';
import { action } from '@ember/object';
import { inject as service } from '@ember/service';
import { tracked } from '@glimmer/tracking';
import { set } from '@ember/object';
import $ from 'jquery';

export default class AccountDetailsController extends Controller {
  @service AjaxUtil;
  @service session;
  @tracked layoutDetails = {};
  @tracked isloading;

  getProfileDetails() {
    var user_id = this.session.data.authenticated.auth_response.user_id;
    var layoutDetails = {};
    this.isloading = 'true';
    this.AjaxUtil.ajax('api/json/action/getProfile', {
      userId: user_id,
    }).then((jsonData) => {
      this.isloading = 'false';
      layoutDetails = {
        userId: user_id,
        fname: jsonData.fname,
        lname: jsonData.lname,
        mail: jsonData.mail,
        mobile: jsonData.mobile,
        gender: jsonData.gender,
        dob: jsonData.dob,
        pincode: jsonData.pincode,
        acc_type: jsonData.acc_type,
        theatres: Object.values(jsonData.theatres || {}),
        lastUsedPincode: '',
      };
      if (pincode != ' ' && pincode != null) {
        this.getLocation(layoutDetails.pincode).then((locations) => {
          layoutDetails.city = locations.city;
          layoutDetails.state = locations.state;
          this.set('layoutDetails', layoutDetails);
        });
      }
    });
  }

  @action
  updateStatusMessage(jsonData) {
    var statusMessage = {
      status: jsonData.status,
      message: jsonData.message,
      show: 'true',
    };
    set(this, 'statusMessage', statusMessage);
  }

  @action
  toggleAccordion(index) {
    const accordionItem = document.querySelectorAll('.accordion-item')[index];
    accordionItem.classList.toggle('active');
  }

  @action
  toggleEdit(className) {
    var isClose = false;
    var selectedClass = $('.' + className);
    const inputs = document.querySelectorAll('.' + className + ' input');
    inputs.forEach((input) => {
      if (input.hasAttribute('disabled')) {
        input.removeAttribute('disabled');
        input.style.border = '1px solid #ccc';
      } else {
        input.setAttribute('disabled', true);
        if (className != 'Personal_details') input.style.border = 'none';
        isClose = true;
      }
    });
    selectedClass.find('.btn-block').toggle();
    selectedClass.find('.edit-btn').toggle();

    if (isClose) {
      const initialLayoutDetails = JSON.parse(
        JSON.stringify(this.layoutDetails)
      );
      this.layoutDetails = initialLayoutDetails;
    }
  }

  @action
  newTheatreRequest(userId, event) {
    event.preventDefault();
    const form = event.target;
    const req_details = {};

    form.querySelectorAll('input').forEach((input) => {
      req_details[input.name] = input.value.trim();
    });
    req_details.userId = userId;

    this.isloading = 'true';
    this.AjaxUtil.ajax('api/json/action/theatreReq', {
      params: JSON.stringify(req_details),
    }).then((jsonData) => {
      this.isloading = 'false';
      this.updateStatusMessage(jsonData);
      if (jsonData.status == 'success') {
        form.reset();
        this.toggleEdit('newThReq');
        this.toggleAccordion(this.layoutDetails.theatres.length);
        this.getProfileDetails();
      }
    });
  }

  @action
  deleteTheatre(theatre_id, event) {
    event.stopPropagation();

    Swal.fire({
      title: 'Are you sure want to delete?',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#3085d6',
      cancelButtonColor: '#d33',
      confirmButtonText: 'Delete',
    }).then((result) => {
      if (result.isConfirmed) {
        this.isloading = 'true';
        this.AjaxUtil.ajax('api/json/action/deleteTheatre', {
          theatre_id: theatre_id,
        }).then((jsonData) => {
          this.isloading = 'false';
          this.updateStatusMessage(jsonData);
          this.getProfileDetails();
        });
      }
    });
  }

  @action
  applyProfileChanges(event) {
    event.preventDefault();
    const form = event.target;
    const profile_details = {};
    form.querySelectorAll('input').forEach((input) => {
      profile_details[input.name] = input.value.trim();
    });

    profile_details.userId = this.layoutDetails.userId;
    const gender = form.querySelector('input[name="gender"]:checked');
    if (gender) profile_details.gender = gender.value;

    this.isloading = 'true';
    this.AjaxUtil.ajax('api/json/action/updateProfileDetails', {
      params: JSON.stringify(profile_details),
    }).then((jsonData) => {
      this.isloading = 'false';
      this.updateStatusMessage(jsonData);
      if (jsonData.status == 'success') {
        this.toggleEdit('Personal_details');
        this.getProfileDetails();
      }
    });
  }

  @action
  location(event) {
    this.checkMaxLength(6, event);
    var pincode = event.target.value;
    if (pincode.length == 6 && this.layoutDetails.lastUsedPincode !== pincode) {
      this.getLocation(pincode).then((locations) => {
        if (event.target.id == 'tpincode') {
          $('#tcity').val(locations.city);
          $('#tstate').val(locations.state);
        } else {
          $('#city').val(locations.city);
          $('#state').val(locations.state);
        }
      });
    }
  }

  async getLocation(pincode) {
    var details = {
      state: 'eg.Tamil Nadu',
      city: 'eg.Chennai',
    };
    if (pincode != ' ' && pincode != null && pincode != '') {
      this.isloading = 'true';
      const jsonData = await this.AjaxUtil.ajax(
        'https://api.postalpincode.in/pincode/' + pincode,
        ''
      );
      this.isloading = 'false';
      if (jsonData && jsonData[0].Status === 'Success') {
        details.state = jsonData[0].PostOffice[0].State;
        details.city = jsonData[0].PostOffice[0].District;
      }
      this.layoutDetails.lastUsedPincode = pincode;
    }
    return details;
  }

  @action
  checkMaxLength(maxlength, event) {
    var value = event.target.value;
    if (value.length > maxlength) {
      value = value.slice(0, maxlength);
      event.target.value = value;
    }
  }

  @action
  editSeatingDetails(event, id) {
    const url = 'http://localhost:4200/seating-details/' + event + '&' + id;
    window.open(
      url,
      '_blank',
      `width=1000,height=1000,left=${(window.screen.width - 1000) / 2},top=${
        (window.screen.height - 1000) / 2
      }`
    );
  }
}
