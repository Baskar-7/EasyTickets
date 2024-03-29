import Component from '@ember/component';
import { action } from '@ember/object';
import $ from 'jquery';
import { tracked } from '@glimmer/tracking';
import { inject as service } from '@ember/service';
import { set } from '@ember/object';
import { computed } from '@ember/object';

export default class HeaderComponent extends Component {
  @service AjaxUtil;
  @tracked details = {};
  @tracked isloading;
  @tracked statusMessage = {};
  @service router;
  @service session;

  init() {
    super.init(...arguments);
    var details = this.AjaxUtil.cookie.locationDetails;
    if (!details) {
      var city_details = [
        'Chennai',
        'Mumbai',
        'Delhi',
        'Bangalore',
        'Hyderabad',
        'Kolkatta',
        'Ahmedabad',
        'Chandigarh',
      ];
      details = {
        mail: '',
        popularCities_details: city_details,
        popular_cities: city_details,
        other_cities: {},
        location:
          this.AjaxUtil.getCookie('location') || this.getCurrentLocation(),
      };
    }
    set(this, 'details', details);
  }

  @computed('session.isAuthenticated')
  get showProfileRoute() {
    return this.session.isAuthenticated;
  }

  @action //toggles the container based on the choice with className
  toggleContainer(className,slideClass) {
    var element = $('.' + className);
  
      element.toggleClass('activate');
      element.toggleClass(slideClass);
    
  }

  @action   //makes a transition to the attempted transistion page
  makeTransition(jsonData) {
    var attemptedTransition = this.session.attemptedTransition;
    if (attemptedTransition) {
      attemptedTransition.retry();
    } else {
      this.updateStatusMessage(jsonData);
    }
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
  selectTab(back, currentTab) {
    var next;
    if (back) next = 1;
    else next = currentTab + 1;
    $('.tab' + currentTab).css('display', 'none');
    $('.tab' + next).css('display', 'block');
  }

  @action
  startTimer() {
    var timeleft = 30;
    var resendTimer = setInterval(function () {
      timeleft--;
      document.getElementById('countdowntimer').textContent =
        'Resend in ' + timeleft + ' seconds';
      if (timeleft <= 0) {
        clearInterval(resendTimer);
        document.getElementById('countdowntimer').textContent = 'Resend OTP';
      }
    }, 1000);
  }

  @action
  sendOTP() {
    var mail = ($('#mailInput').val()).trim();
    if (mail != '' && mail != undefined) {
      var details = this.details;
      details.mail = mail;
      this.set('details', details);

      this.isloading = 'true';
      this.AjaxUtil.ajax('api/json/action/sendOTP', {
        mailId: this.details.mail,
      }).then((jsonData) => {
        this.isloading = 'fasle';
        if (jsonData.status == 'success') {
          $('#mailInput').val('');
          this.startTimer();
          this.selectTab(false, 2);
        }
        this.updateStatusMessage(jsonData);
      });
    }
    return false;
  }

  @action
  ResendOTP() {
    var val = $('#countdowntimer').text();
    if (!val.localeCompare('Resend OTP')) {
      this.sendOTP();
      var inputbox = document.getElementById('OTPContainer').firstElementChild;
      inputbox.value = '';
      while (inputbox.nextElementSibling) {
        inputbox = inputbox.nextElementSibling;
        inputbox.setAttribute('disabled', true);
        inputbox.value = '';
      }
    }
  }

  @action
  OTPContainer(event) {
    const currentInput = event.target,
      nextInput = event.target.nextElementSibling,
      prevInput = event.target.previousElementSibling;

    if (currentInput.value.length > 1 || event.keyCode == 69) {
      currentInput.value = '';
      return;
    }
    if (
      nextInput &&
      nextInput.hasAttribute('disabled') &&
      currentInput.value !== ''
    ) {
      nextInput.removeAttribute('disabled');
      currentInput.setAttribute('disabled', true);
      nextInput.focus();
    }

    if (event.key === 'Backspace' && prevInput) {
      currentInput.setAttribute('disabled', true);
      prevInput.removeAttribute('disabled');
      currentInput.value = '';
      prevInput.focus();
    }
  }

  @action
  async verifyOTP(event) {
    event.preventDefault();
    var inputs = document.querySelectorAll('input[type="number"]');
    var OTP = '';
    inputs.forEach(function (input) {
      OTP += input.value;
    });
    this.isloading = 'true';

    try {
      await this.session.authenticate(
        'authenticator:custom',
        (this.details.mail).trim(),
        OTP
      );
      console.log(
        'authentication successfull: ' + this.session.isAuthenticated
      );
    } catch (error) {
      console.log('Authentication failed...');
    }
    var jsonData = this.session.data.authenticated.auth_response;
    console.log(jsonData);

    this.isloading = 'fasle';
    if (jsonData && jsonData.status == 'success') {
      this.toggleContainer( 'login-container','slidetoTop');
      this.selectTab(true, 3);
      this.makeTransition(jsonData);
    } else
      this.updateStatusMessage({
        status: 'error',
        message: 'The code you entered is incorrect. Please try again',
      });
  }

  @action
  async getLocations() {
    this.isloading = 'true';
    var locations = await this.AjaxUtil.getLocations();
    this.isloading = 'false';
    if (locations.length > 0) {
      set(this, 'details.other_cities', locations);
      this.AjaxUtil.cookie.locationDetails = this.details;
    }
  }

  @action
  async searchOtherCitiesLocation() {
    let location = $('#location').val();
    this.searchPopularCitiesLocation(location);
    var cities = [];
    var city_details = await this.AjaxUtil.getLocations();
    if (city_details) {
     city_details.forEach((city)=> {
        if (
          city.district.toLowerCase().indexOf(location.toLowerCase()) !== -1
        ) {
          cities.push(city);
        }
      })
    }
    this.set('details.other_cities', cities);
  }

  searchPopularCitiesLocation(location) {
    var cities = [];
    var city_details = this.details.popularCities_details;
    for (var i = 0; i < city_details.length; i++) {
      var city = city_details[i];
      if (city.toLowerCase().indexOf(location.toLowerCase()) !== -1) {
        cities.push(city);
      }
    }
    this.set('details.popular_cities', cities);
  }

  @action
  selectLocation(location) {
    this.AjaxUtil.addCookie('location', location, 1);
    this.toggleContainer( 'locations','slidetoRight');
    this.set('details.location',location)
    if(this.router.currentRouteName == 'index')
      this.action('searchShows', '');
    else
      this.router.transitionTo('index'); 
  }

  @action
  clearSearch() {
    $('#location').val('');
     this.getLocations();
  }

  @action
  searchShow(event) {
    if (event.keyCode == 13 || event.target.value == "") {
      if(this.router.currentRouteName == 'index')
        this.action('searchShows', event.target.value);
      else 
        this.router.transitionTo('index');
    }
  }

  @action
  getCurrentLocation() {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          this.isloading = 'true';
          const apiUrl = `https://api.opencagedata.com/geocode/v1/json?key=652cda7aaba746f4ad96a711820c28ad&q=${position.coords.latitude}+${position.coords.longitude}&pretty=1`;
          this.AjaxUtil.ajax(apiUrl).then((data) => {
            this.isloading = 'false';
            var location=(data.results[0]?.components.state_district.split(' '))[0] || data.results[0]?.components.town
            this.selectLocation(
              location
            );
          });
        },
        (error) => {
          console.error('Error getting location:', error.message);
        }
      );
    }
  }

  @action
  googleAuth() {
    const googleSignInUrl =
      'https://accounts.google.com/gsi/button?type=standard&client_id=1076290514959-jvfdbfffrka5isr859dr447je2i6cc4u.apps.googleusercontent.com&iframe_id=gsi_84872_764221&as=d%2F3aIC%2BSjnW8c4Zd8dclRQ';
    window.open(
      googleSignInUrl,
      '_blank',
      `width=500,height=500,left=${(window.screen.width - 500) / 2},top=${
        (window.screen.height - 500) / 2
      }`
    );
    // window.addEventListener('message', this.receiveMessage);
  }

  // async googleAuth() {
  //   try {
  //     const googleUser = await this.googleSignIn.signIn();
  //     this.onSignIn(googleUser);
  //   } catch (error) {
  //     console.error('Google Sign-In Error:', error);
  //   }
  // }


  @action
  onSignIn(googleUser) {
    alert('onSignin');
    var profile = googleUser.getBasicProfile();
    console.log('ID: ' + profile.getId());
    console.log('Name: ' + profile.getName());
    console.log('Image URL: ' + profile.getImageUrl());
    console.log('Email: ' + profile.getEmail()); // This is null if the 'email' scope is not present.
  }

  @action
  printConsole(param) {
    console.log(param);
  }
}
