import Controller from '@ember/controller';
import { inject as service } from '@ember/service';
import { tracked } from '@glimmer/tracking';
import { action } from '@ember/object';
import { set } from '@ember/object';
import $ from 'jquery';

export default class IndexController extends Controller {
  /*
    It is the home page of the application where it display the shows that are hosted by a theatre owners and also it displays the upcoming movies that going to be realeased upto 30days...
  */

  @service AjaxUtil; //makes any http request to the server
  @tracked isloading; //creates a loading effect to the user using a component <-- Loader -->
  @tracked scrollRight = true; //tracks the scrolling side of the upcoming movies container
  @tracked layout_details = {}; //stores all the information of the current page

  //initialize the required things
  async initialize(upcoming_movies) {
    super.init(...arguments);
    this.isloading = 'false';
    var location = this.AjaxUtil.getCookie('location');
    var layout_details = {
      searchText: '',
      location: location || this.getCurrentLocation(),
      filterList: [],
      show_details: {},
      time_intervals: '',
      upcoming_movielist: upcoming_movies,
      upcoming_movies: upcoming_movies,
    };
    this.set('layout_details', layout_details);
    this.startScroller();
  }

  @action //makes an automative scroller for the upcoming movies container
  scrollLeft() {
    var scrollingDiv = document.querySelector('.UM_container');
    var scroll_amount = -600;

    if (scrollingDiv.scrollLeft == 0) {
      this.scrollRight = true;
    } else if (
      Math.round(scrollingDiv.scrollLeft) -
        (scrollingDiv.scrollWidth - scrollingDiv.clientWidth) == 0
    ) {
      this.scrollRight = false;
    }
    if (this.scrollRight) {
      scroll_amount = 600;
    }
    scrollingDiv.scrollBy({
      left: scroll_amount,
      behavior: 'smooth',
    });
  }

  @action //start upcoming makes scroller by simply start timeInterval to make movable
  startScroller()
  {
    if (this.layout_details.upcoming_movies.length > 1)
      this.layout_details.time_intervals = setInterval(this.scrollLeft, 2500);
  }

  @action   //reset time properties or clear the time intervals
  resetProperties() {
    if(this.layout_details.time_intervals)
      clearInterval(this.layout_details.time_intervals);
    if(this.layout_details.form_timer)
      clearInterval(this.layout_details.form_timer);
  }

  @action //resets all the form properties of the request show form
  resetFormProperties() {
    clearInterval(this.layout_details.form_timer);
    $('.otp-container').css('display', 'none');
    $('#get-otp-button').css('display', 'block');
    $('#get-otp-button').text('Get OTP');
    $('.verified-note').css('display', 'none');
    var mail_ele = document.getElementById('mail');
    mail_ele.removeAttribute('disabled');
    mail_ele.style.border = 'none';
    var inputs = document.querySelectorAll('#request-form input');
    inputs.forEach(function (input) {
      input.value = '';
    });
    this.layout_details.lastUsedPincode = '';
  }

  @action //updates the status message to the user using the component <-- MessageBox -->
  updateStatusMessage(jsonData) {
    var statusMessage = {
      status: jsonData.status,
      message: jsonData.message,
      show: 'true',
    };
    set(this, 'statusMessage', statusMessage);
  }

  @action //a toggle class to stop the bounce effect of the listshow-icon
  toggleBounceEffect() {
    $('.listshow-icon').toggleClass(' pause');
  }

  @action //toggles the container based on the choice with className
  toggleContainer(className,slideClass) {
    var element = $('.' + className);
  
      element.toggleClass('activate');
      element.toggleClass(slideClass);
  }

  @action //toggle Dropdown witht the provided classNames
  toggleDropdown(className, option) {
    var dropdown = $('.' + className);
    if (option == 'close') {
      dropdown.removeClass('active');
      dropdown.find('.dropdown-menu').slideUp(300);
    } else {
      dropdown.toggleClass('active');
      dropdown.find('.dropdown-menu').slideToggle(300);
    }
    document.querySelector('.' + className + ' > .dropdown-menu').scrollTop = 0;
  }

  @action //get the updated shows from the db with the filter options
  getShows() {
    this.isloading = 'true';
    var layout_details = this.layout_details;
    var params = {
      searchText: layout_details.searchText,
      filters: this.layout_details.filterList,
      location: this.AjaxUtil.getCookie('location'),
    };
    this.AjaxUtil.ajax('api/json/action/getShows', {
      params: JSON.stringify(params),
    }).then((jsonData) => {
      this.isInitialized = 'true';
      this.isloading = 'false';
      layout_details.show_details = jsonData;
      layout_details.languages = jsonData.languages;
      layout_details.genres = jsonData.genres;
      this.set('layout_details', layout_details);
    });
  }

  @action //toggles show container and reset the properties
  requestForShow() {
    this.toggleContainer('host_request_container','slidetoTop');
    this.resetFormProperties();
  }

  @action // makes an ajax call to post the user host request
  addNewRequest(event) {
    event.preventDefault();
    if (this.layout_details.isVerified) {
      const form = event.target;
      const values = {};

      form.querySelectorAll('input').forEach((input) => {
        values[input.name] = input.value.trim();
      });

      this.isloading = 'true';
      this.AjaxUtil.ajax('api/json/action/hostRequest', {
        params: JSON.stringify(values),
      }).then((jsonData) => {
        this.isloading = 'fasle';
        if (jsonData.status != 'info') this.requestForShow('close');
        this.updateStatusMessage(jsonData);
      });
    } else {
      $('#get-otp-button').css('border', '2px solid rgb(222, 115, 115)');
    }
  }

  @action // makes an ajax call to send the otp to the provided user mail
  sendOtp() {
    var mail = $('#mail').val();
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailPattern.test(mail)) {
      $('#mail').css('border', '2px solid rgb(222, 115, 115)');
    } else {
      $('#mail').css('border', 'none');
      var btnText = $('#get-otp-button').text().trim();

      if (btnText === 'Resend OTP' || btnText === 'Get OTP') {
        this.isloading = 'true';
        this.AjaxUtil.ajax('api/json/action/sendOTP', {
          mailId: mail,
        }).then((jsonData) => {
          this.isloading = 'fasle';
          if (jsonData.status == 'success') {
            this.startTimer();
            $('#get-otp-button').css('border', '1px solid #CCCCCC');
            $('.otp-container').css('display', 'flex');
            this.layout_details.mail = mail;
          }
          this.updateStatusMessage(jsonData);
        });
      }
    }
  }

  @action //makes an ajax call to verify the otp of the hostrequest mail
  verifyOtp() {
    var otpContainer = document.getElementById('otp-box');
    var currentOtp = otpContainer.value;
    if (currentOtp == '') {
      $('#otp-box').css('border', 'none');
    } else if (
      currentOtp.length == '4' &&
      this.layout_details.lastOtp !== currentOtp
    ) {
      otpContainer.blur();
      this.AjaxUtil.ajax('api/json/action/verifyCredentials', {
        params: JSON.stringify({
          credential: this.layout_details.mail,
          otp: currentOtp,
        }),
      }).then((jsonData) => {
        if (jsonData.status == 'success') {
          $('#get-otp-button').css('display', 'none');
          $('.verified-note').css('display', 'block');
          $('.otp-container').css('display', 'none');
          $('#mail').css('border', 'none');
          var mail_ele = document.getElementById('mail');
          mail_ele.setAttribute('disabled', true);
          this.layout_details.isVerified = true;
        }
        this.layout_details.lastOtp = currentOtp;
        this.updateStatusMessage(jsonData);
      });
    } else if (currentOtp.length != 4) {
      $('#otp-box').css('border', '2px solid rgb(222, 115, 115)');
    }
  }

  @action //select filter for the movies using dropdown
  selectFilter(filterType, filterValue, filterCode) {
    this.toggleDropdown(filterType + '-list', 'close');
    var filters = this.layout_details.filterList;
    var filterIndex = filters.findIndex(
      (filter) => filter.filterValue === filterValue
    );

    if (filterIndex == -1) {
      var filter = {
        filterType: filterType,
        filterValue: filterValue,
        filterCode: filterCode,
      };
      filters.push(filter);
      this.filterUpcomingMovies(filters);
      this.set('layout_details.filterList', []);
      this.set('layout_details.filterList', filters);
      this.getShows();
    }
  }

  @action //filter upcoming movies according to the filters
  filterUpcomingMovies(filters) {
    var movies = [];
    var upcoming_movies = this.layout_details.upcoming_movielist;

    upcoming_movies.forEach((movie) => {
      var isExist = false;
      filters.forEach((filter) => {
        const { filterType, filterCode } = filter;
        if (filterType == 'genre') {
          if (movie.genre_ids.includes(filterCode)) isExist = true;
        } else {
          if (movie.original_language === filterCode) isExist = true;
        }
      });
      if (isExist && !movies.includes(movie)) {
        movies.push(movie);
      }
    });

    if (movies.length == 0 && filters.length == 0) {
      movies = upcoming_movies;
    }

    this.set('layout_details.upcoming_movies', movies);
  }

  @action //search inside the dropdown with the given filter
  searchFilters(filterType, event) {
    var reqFilter = event.target.value;
    var filterList = this.layout_details.show_details[filterType];
    var filters = [];
    for (var i = 0; i < filterList.length; i++) {
      var filter = filterList[i];
      if (filter.toLowerCase().indexOf(reqFilter.toLowerCase()) != -1) {
        filters.push(filter);
      }
    }
    this.set('layout_details.' + [filterType], filters);
  }

  @action //remove the filter applied to the movies
  removeFilter(filter) {
    var filters = this.layout_details.filterList;
    var filteredFilters = filters.filter(
      (Filter) => Filter.filterValue !== filter
    );
    this.filterUpcomingMovies(filteredFilters);
    this.set('layout_details.filterList', filteredFilters);
    this.getShows();
  }

  @action //removes all the filters applied via "clear ALL"
  removeAllFilters() {
    this.filterUpcomingMovies([]);
    this.set('layout_details.filterList', []);
    this.getShows();
  }

  @action //handle action for searching movies from the Header Component
  handleActions(actionName, params) {
    if (actionName == 'searchShows') {
      this.layout_details.searchText = params || document.getElementById('movie_search_box').value;console.log(this.layout_details.searchText)
      this.getShows();
    }
  }

  @action //selects the user location and save it to the cookie for future reference
  selectLocation(location) {
    this.AjaxUtil.addCookie('location', location, 30);
    this.toggleContainer('locations', 'slidetoRight');
    $('.location span').text(location);
  }

  @action //locate the current location using openweatherapi.org...
  getCurrentLocation() {
    if (navigator.onLine) {
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            this.isloading = 'true';
            const apiUrl = `https://api.opencagedata.com/geocode/v1/json?key=652cda7aaba746f4ad96a711820c28ad&q=${position.coords.latitude}+${position.coords.longitude}&pretty=1`;
            this.AjaxUtil.ajax(apiUrl).then((data) => {
              this.isloading = 'false';
              this.selectLocation(
                (data.results[0]?.components.state_district.split(' '))[0]
              );
            });
          },
          (error) => {
            console.error('Error getting location:', error.message);
          }
        );
      }
    } else {
      var json = {
        status: 'info',
        message: 'No Internet Connection',
      };
      this.updateStatusMessage(json);
    }
  }

  @action //get city and state using the pincode via an ajax call to the api.postalpincode.in
  getLocation(event) {
    var pincode = event.target.value;
    this.checkMaxLength(6, event);

    if (
      pincode.length == 6 &&
      this.layout_details.lastUsedPincode !== pincode
    ) {
      this.isloading = 'true';
      this.AjaxUtil.ajax(
        'https://api.postalpincode.in/pincode/' + pincode,
        ''
      ).then((jsonData) => {
        this.isloading = 'false';
        var state = '',
          city = '';
        if (jsonData && jsonData[0].Status === 'Success') {
          state = jsonData[0].PostOffice[0].State;
          city = jsonData[0].PostOffice[0].District;
        } else {
          this.updateStatusMessage({
            status: 'error',
            message: 'Invalid Pincode...',
          });
          event.target.value = '';
        }
        this.layout_details.lastUsedPincode = pincode;
        $('#city').val(city);
        $('#state').val(state);
      });
    }
  }

  @action //uses a setInterval to run a resend otp timer
  startTimer() {
    var timeleft = 30;

    this.layout_details.form_timer = setInterval(function () {
      timeleft--;
      document.getElementById('get-otp-button').textContent =
        timeleft + ' seconds';
      if (timeleft <= 0) {
        clearInterval(this.layout_details.form_timer);
        document.getElementById('get-otp-button').textContent = 'Resend OTP';
      }
    }, 1000);
  }

  @action //used as an helper that checks the maxlength with the event target value
  checkMaxLength(maxlength, event) {
    var value = event.target.value;
    if (value.length > maxlength) {
      value = value.slice(0, maxlength);
      event.target.value = value;
    }
  }

  @action // uses for the debug for arrays,objects,etc...
  printConsole(param) {
    console.log(param);
  }
}
