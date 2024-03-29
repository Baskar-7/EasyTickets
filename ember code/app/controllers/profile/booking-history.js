import Controller from '@ember/controller';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';
import { inject as service } from '@ember/service';
import { set } from '@ember/object';
import $ from 'jquery';

export default class BookingHistoryController extends Controller {
  @service session;
  @service AjaxUtil;
  @tracked history_details;
  @tracked layout_details;
  @tracked isloading = 'false';

  @action
  initializeDetails() {

    var layout = {
      cols: ["Screen Name","Show Date&Time","Available Tickets","Total Earnings","Status"],
      searchBar:false,
      actions: ['delete','lock','unlock'],
      actionsFor: {
        delete: { 'Status': 'Expired' },
        lock: {'Status' : 'Open'},
        unlock : {'Status' : 'Closed'}
      },
      label: 'history',
      histories: []
    };

    var history_details = {
      user_id: this.session.data.authenticated.auth_response.user_id,
      booking_history: { filter_value: { from: '', to: '' }, histories: {}, for_filter: {}},
      show_history : {histories : [], layout_details: layout}
    };
    this.set('history_details', history_details);
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
  getBookingHistories() {
    var history = this.history_details.booking_history;
    var params = {
      user_id: this.history_details.user_id,
      from: history.filter_value.from,
      to: history.filter_value.to,
    };
    this.isloading = 'true';
    this.AjaxUtil.ajax('api/json/action/getBookingHistories', {
      params: JSON.stringify(params),
    }).then((jsonData) => {
      this.isloading = 'false';
      var history_details = {
        histories: jsonData.histories,
        filter_value: history.filter_value,
        for_filter: jsonData.histories,
      };
      this.set('history_details.booking_history', history_details);
    });
  }

  @action 
  getShowHistories()
  {
    var params={
      user_id : this.history_details.user_id
    }
    this.isloading = 'true';
    this.AjaxUtil.ajax('api/json/action/getShowHistories', {
      params: JSON.stringify(params),
    }).then((jsonData) => {
      this.isloading='false';
      var history_details = {
        histories: jsonData.histories,
        layout_details: this.history_details.show_history.layout_details
      };
      this.set('history_details.show_history',history_details);
    });
  }

  @action
  toggleDropdown(event) {
    var dropdown = $('.dropdown');
    if (event.type == 'focusout') {
      dropdown.removeClass('active');
      dropdown.find('.dropdown-menu').slideUp(300);
    } else {
      dropdown.toggleClass('active');
      dropdown.find('.dropdown-menu').slideToggle(300);
    }
  }

  @action
  getThisWeekDates() {
    const currentDate = new Date();
    const currentDay = currentDate.getDay();
    const daysUntilSunday = currentDay === 0 ? 0 : 7 - currentDay;
    const nextSunday = new Date(currentDate);
    nextSunday.setDate(currentDate.getDate() + daysUntilSunday);
    const previousMonday = new Date(currentDate);
    previousMonday.setDate(currentDate.getDate() - currentDay);

    return {
      startOfWeek: previousMonday,
      endOfWeek: nextSunday,
    };
  }

  @action
  getThisMonthDates() {
    const currentDate = new Date();
    const firstDayOfMonth = new Date(
      currentDate.getFullYear(),
      currentDate.getMonth(),
      1
    );
    const lastDayOfMonth = new Date(
      currentDate.getFullYear(),
      currentDate.getMonth() + 1,
      0
    );

    return {
      startOfMonth: firstDayOfMonth,
      endOfMonth: lastDayOfMonth,
    };
  }

  @action
  filterBy(filter_type, event) {
    var date1 = '',
      date2 = '';

    if (filter_type == 'date') {
      var from = new Date(document.getElementById('from').value),
        to = new Date(document.getElementById('to').value);
      if (from != 'Invalid Date') {
        if (to != 'Invalid Date' && from > to) {
          document.getElementById('to').value = '';
          return;
        } else if (to != 'Invalid Date') {
          date1 = `${from.getFullYear()}-${
            from.getMonth() + 1
          }-${from.getDate()}`;
          date2 = `${to.getFullYear()}-${to.getMonth() + 1}-${to.getDate()}`;
        } else {
          return;
        }
      } else {
        return;
      }

      document.getElementById(
        'selected_filter'
      ).innerText = `Filter By: ${from.getDate()}/${
        from.getMonth() + 1
      }/${from.getFullYear()}-${to.getDate()}/${
        to.getMonth() + 1
      }/${to.getFullYear()}`;
      this.toggleClass('date-filter', 'slide', 'toggle');
    } else if (filter_type == 'week') {
      const currentDate = new Date();
      const currentDay = currentDate.getDay();
      const daysUntilSunday = currentDay === 0 ? 0 : 7 - currentDay;
      const nextSunday = new Date(currentDate);
      nextSunday.setDate(currentDate.getDate() + daysUntilSunday);
      const previousMonday = new Date(currentDate);
      previousMonday.setDate(currentDate.getDate() - currentDay);

      date1 = `${previousMonday.getFullYear()}-${
        previousMonday.getMonth() + 1
      }-${previousMonday.getDate()}`;
      date2 = `${nextSunday.getFullYear()}-${
        nextSunday.getMonth() + 1
      }-${nextSunday.getDate()}`;
      document.getElementById('selected_filter').innerText =
        'Filter By: This Week';
    } else if (filter_type == 'month') {
      const currentDate = new Date();
      const firstDayOfMonth = new Date(
        currentDate.getFullYear(),
        currentDate.getMonth(),
        1
      );
      const lastDayOfMonth = new Date(
        currentDate.getFullYear(),
        currentDate.getMonth() + 1,
        0
      );
      date1 = `${firstDayOfMonth.getFullYear()}-${
        firstDayOfMonth.getMonth() + 1
      }-${firstDayOfMonth.getDate()}`;
      date2 = `${lastDayOfMonth.getFullYear()}-${
        lastDayOfMonth.getMonth() + 1
      }-${lastDayOfMonth.getDate()}`;
      document.getElementById('selected_filter').innerText =
        'Filter By: This Month';
    } else {
      document.getElementById('selected_filter').innerText =
        'Filter By: All Bookings';
    }

    this.history_details.booking_history.filter_value = {
      from: date1,
      to: date2,
    };
    this.toggleDropdown(event);
    this.getBookingHistories();
    document.getElementById('from').value = '';
    document.getElementById('to').value = '';
  }

  @action
  toggleClass(id, className, event) {
    if (event == 'toggle') {
    } else if (event.target.tagName.toLowerCase() === 'input') {
      return;
    }
    document.getElementById(id).classList.toggle(className);
  }

  @action
  toggleAccordion(index) {
    const accordionItem = document.querySelectorAll('.accordion-item')[index];
    accordionItem.classList.toggle('active');
  }

  @action
  search(event) {
    this.set('history_details.booking_history.histories' + []);
    var search_value = event.target.value;
    var filterList = this.history_details.booking_history.for_filter;
    var filter={
      valid_tickets : this.filter(filterList.valid_tickets,search_value),
      expired_tickets: this.filter(filterList.expired_tickets,search_value)
    }
    this.set('history_details.booking_history.histories', filter);
  }

  @action
  filter(filterList,search_value)
  {
    var filters = [];
    for (var i = 0; i < filterList.length; i++) {
      var filter = filterList[i].movie_name;
      if (filter.toLowerCase().indexOf(search_value.toLowerCase()) != -1) {
        filters.push(filterList[i]);
      }
    }
    return filters;
  }

  @action
  handleActions(action,param)
  {
    if(action == 'view')
    {
      console.log("view enhanced details")
    }
    else if(action == 'delete')
    {
      Swal.fire({
        title: 'Are you sure want to Delete ?',
        text: "You won't be able to revert this!",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#3085d6',
        cancelButtonColor: '#d33',
        confirmButtonText: 'Yes, delete it!',
      }).then((result) => {
        if (result.isConfirmed) {
          var params={
            user_id : this.history_details.user_id,
            history_id : param.history_id
          }
          this.isloading = 'true';
          this.AjaxUtil.ajax('api/json/action/deleteShowHistory', { params: JSON.stringify(params)}).then((jsonData) => {
            this.isloading = 'false';
            this.updateStatusMessage(jsonData);
            if (jsonData.status == 'success') this.getShowHistories();
          });
        }
      });
    }
    else if(action == 'lock')
    {
      Swal.fire({
        title: 'Are you sure want to Close the Booking ?',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#3085d6',
        cancelButtonColor: '#d33',
        confirmButtonText: 'Yes!',
      }).then((result) => {
        if (result.isConfirmed) {
           this.toggleShowStatus({status : 'Closed', history_id : param.history_id})
        }
      });
    }
    else if(action == 'unlock')
    {
      Swal.fire({
        title: 'Are you sure want to Open the Booking ?',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#3085d6',
        cancelButtonColor: '#d33',
        confirmButtonText: 'Yes!',
      }).then((result) => {
        if (result.isConfirmed) {
           this.toggleShowStatus({status : 'Open', history_id : param.history_id})
        }
      });
    }
  }

  @action 
  toggleShowStatus(params)
  {
    this.isloading = 'true';
    this.AjaxUtil.ajax('api/json/action/toggleShowStatus', { params: JSON.stringify(params)}).then((jsonData) => {
      this.isloading = 'false';
      this.updateStatusMessage(jsonData);
      if (jsonData.status == 'success')
        this.getShowHistories();
    });
  }

  @action
  printConsole(param)
  {
    console.log(param)
  }
}
