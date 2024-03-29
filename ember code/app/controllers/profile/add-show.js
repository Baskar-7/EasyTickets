import Controller from '@ember/controller';
import { action } from '@ember/object';
import $ from 'jquery';
import { tracked } from '@glimmer/tracking';
import { inject as service } from '@ember/service';
import { set } from '@ember/object';

export default class AddShowController extends Controller {
  @service AjaxUtil;
  @service session;
  @tracked isloading = 'false';
  @tracked addShow = {};

  @action
  printConsole(param) {
    console.log(param);
  }

  initializeDetails() {
    this.set('addShow', {});
    $('.movie-list span').text('Select Movie');
    $('#title').val('');
    this.isloading = 'true';
    this.AjaxUtil.ajax('api/json/action/getTheatres', {
      userId: this.session.data.authenticated.auth_response.user_id,
    }).then((jsonData) => {
      this.isloading = 'false';
      if (jsonData.status != 'error') {
        var theatreDetails = {
          theatres: Object.values(jsonData.theatres || {}),
          screens: jsonData.screens,
        };
        this.set('addShow.theatreDetails', theatreDetails || {});
      }
    });
  }

  @action
  setDefault() {
    var shows = {
      showCount: [this.addShow.details.imdb_id + '0'],
      showDetails: {},
    };
    this.set('addShow.shows', shows);
  }

  @action
  toggleBtn(className) {
    $('.' + className).toggle();
  }

  updateStatusMessage(jsonData) {
    var statusMessage = {
      status: jsonData.status,
      message: jsonData.message,
      show: 'true',
    };
    set(this, 'statusMessage', statusMessage);
  }

  @action
  toggleDropdown(className, event) {
    if (this.addShow.list && this.addShow.list.length > 0) {
      var dropdown = $('.' + className);
      if (event.type == 'focusout') {
        dropdown.removeClass('active');
        dropdown.find('.dropdown-menu').slideUp(300);
      } else {
        dropdown.toggleClass('active');
        dropdown.find('.dropdown-menu').slideToggle(300);
      }
    }
  }

  @action
  slide(slideTo, event) {
    event.preventDefault();
    const slide1 = document.querySelector('.slide1');
    const slide2 = document.querySelector('.slide2');
    if (slideTo === 'slide2') {
      slide1.style.transform = 'translateX(-200vh)';
      slide2.style.transform = 'translateX(-145vh)';
    } else {
      slide1.style.transform = 'translateX(0vh)';
      slide2.style.transform = 'translateX(100vh)';
    }
  }

  @action
  selectMovie(event) {
    var movieId = event.target.id;
    $('.movie-list span').text($(event.target).text());
    this.toggleDropdown('movie-list', 'focusout');
    this.isloading = 'true';
    this.AjaxUtil.getMovieDetails(movieId).then((details) => {
      if (details.status === 'info') {
        this.updateStatusMessage(details);
      } else {
        if (this.addShow.details) this.slide('slide1', event);

        this.set('addShow.details', details);
        this.setDefault(event);
      }
      this.isloading = 'false';
    });
  }

  @action
  SearchMovie(event) {
    if (event.keyCode == 13) {
      this.isloading = 'true';
      this.AjaxUtil.tmdbApi(event.target.value).then((data) => {
        this.isloading = 'false';
        if (data.status === 'info') {
          this.updateStatusMessage(data);
        } else {
          var list = data;
          this.set('addShow.list', list);
          this.toggleDropdown('movie-list', 'click');
        }
      });
    }
  }

  @action
  deleteShow(rowId) {
    const rowToDelete = $('.show_' + rowId);
    rowToDelete.remove();
    delete this.addShow.shows.showDetails[rowId];
  }

  @action
  addShowsDetails(rowId, event) {
    const showRow = document.querySelector('.show_' + rowId);
    const inputElements = showRow.querySelectorAll('input, select');
    var inputs = {};
    inputElements.forEach((element) => {
      if (
        (element.tagName === 'INPUT' || element.tagName === 'SELECT') &&
        element.value != ''
      ) {
        var name = element.name;
        if (name === 'show_time&date') {
          var timing = element.value.split('T');
          inputs['show_startDate'] = timing[0];
          inputs['show_startTime'] = timing[1];
        } else if (name == 'show_endTime') {
          inputs['show_endTime'] = element.value;
          var timeParts = element.value.split(':');
          if (timeParts[0] == '00') {
            inputs['show_endDate'] = this.getLocalDate(
              this.parseTime(element.value, inputs['show_startDate'], true)
            );
          } else {
            inputs['show_endDate'] = this.getLocalDate(
              inputs['show_startDate']
            );
          }
        } else inputs[name] = element.value;
      }
    });

    if (Object.keys(inputs).length == 12) {
      event.preventDefault();
      var clash = false;
      var showDetails = this.addShow.shows.showDetails,
        details = {},
        msg = {
          status: 'error',
          message: 'Screen reserved for another show at this time!...',
        };
      var showSt = this.parseTime(
          inputs['show_startTime'],
          inputs['show_startDate'],
          false
        ),
        showEt = this.parseTime(
          inputs['show_endTime'],
          inputs['show_startDate'],
          true
        );
      for (var val of Object.values(showDetails)) {
        if (val.theatre_id == inputs.theatre_id) {
          details = val;
        }
        var exShowSt = this.parseTime(
            details['show_startTime'],
            details['show_startDate'],
            false
          ),
          exShowEt = this.parseTime(
            details['show_endTime'],
            details['show_startDate'],
            true
          );
        if (
          Object.keys(showDetails).length !== 0 &&
          details['screen_id'] == inputs['screen_id'] &&
          exShowSt < showEt &&
          exShowEt > showSt
        ) {
          clash = true;
          this.updateStatusMessage(msg);
          break;
        }
      }
      if (!clash) {
        this.checkShowClash(inputs, showDetails, msg, rowId);
      }
    }
  }

  @action
  getLocalDate(inputDateString) {
    const inputDate = new Date(inputDateString);
    const options = {
      timeZone: 'Asia/Kolkata',
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    };
    inputDate.toLocaleString('en-IN', options);
    const day = String(inputDate.getDate()).padStart(2, '0');
    const month = String(inputDate.getMonth() + 1).padStart(2, '0');
    const year = inputDate.getFullYear();
    return `${day}-${month}-${year}`;
  }

  @action
  parseTime(timeString, date, end_date) {
    var timeParts = timeString.split(':');
    var parsedTime = new Date(date);
    if (timeParts[0] == '00' && end_date) {
      parsedTime.setHours(parseInt(24, 10));
    } else {
      parsedTime.setHours(parseInt(timeParts[0], 10));
    }
    parsedTime.setMinutes(parseInt(timeParts[1], 10));
    parsedTime.setSeconds(0);
    parsedTime.setMilliseconds(0);
    return parsedTime;
  }

  @action
  checkShowClash(inputs, showDetails, msg, rowId) {
    inputs['movie_id'] = this.addShow.details.imdb_id;
    this.isloading = 'true';
    this.AjaxUtil.ajax('api/json/action/checkShowClash', {
      params: JSON.stringify(inputs),
    }).then((jsonData) => {
      this.isloading = 'false';
      if (jsonData.status == 'error') {
        this.updateStatusMessage(msg);
      } else {
        showDetails[rowId] = inputs;
        this.addShow.shows.showDetails = showDetails;
        this.toggleBtn('add-btn_' + rowId);
        this.disableInputs('show_' + rowId);
        msg = {
          status: 'success',
          message: 'Show Details added successfully!!',
        };
        this.updateStatusMessage(msg);
      }
    });
  }

  @action
  confirmShowBookings(event) {
    event.preventDefault();
    var shows = [];
    for (var data of Object.values(this.addShow.shows.showDetails)) {
      shows.push(data);
    }

    var params = {
      movie_details: this.addShow.details,
      shows: shows,
    };

    this.isloading = 'true';
    this.AjaxUtil.ajax('api/json/action/addShows', {
      params: JSON.stringify(params),
    }).then((jsonData) => {
      this.isloading = 'false';
      this.updateStatusMessage(jsonData);
      this.initializeDetails();
    });
  }

  @action
  disableInputs(className) {
    const inputs = document.querySelectorAll(
      '.' + className + ' input, .' + className + ' select'
    );
    inputs.forEach((element) => {
      element.disabled = true;
    });
  }

  @action
  getEndTime(rowId, event) {
    var formattedEndTime = '';
    if (event.target.value) {
      const startTime = event.target.value.split('T')[1];
      var [startHour, startMinute] = startTime.split(':').map(Number);
      var totalMinutes =
        startHour * 60 + startMinute + this.addShow.details.runtime;
      var endHour = Math.floor(totalMinutes / 60);
      while (endHour >= 24) {
        endHour = endHour - 24;
      }
      formattedEndTime =
        String(endHour).padStart(2, '0') +
        ':' +
        String(totalMinutes % 60).padStart(2, '0');
    }
    $('#endTime_' + rowId).val(formattedEndTime);
  }

  @action
  increaseShowCount() {
    var showCount = this.addShow.shows.showCount;

    showCount.push(this.addShow.details.imdb_id + '' + showCount.length);
    var shows = {
      showCount: showCount,
      showDetails: this.addShow.shows.showDetails,
    };
    this.set('addShow.shows', shows);
  }

  @action
  selectOption(row, event) {
    var theatre_id = event.target.value;
    var dropdown = document.getElementById('screen_' + row);
    dropdown.innerHTML = '';
    var optionElement = document.createElement('option');
    optionElement.value = '';
    optionElement.text = 'Select Screen';
    dropdown.appendChild(optionElement);
    if (theatre_id == '') {
      dropdown.disabled = true;
    } else {
      var screen_details = this.addShow.theatreDetails.screens[theatre_id];
      dropdown.disabled = false;
      screen_details.forEach((option) => {
        optionElement = document.createElement('option');
        optionElement.value = option.screen_id;
        optionElement.text = option.screen_name;
        dropdown.appendChild(optionElement);
      });
    }
  }
}
