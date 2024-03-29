import Controller from '@ember/controller';
import { action } from '@ember/object';
import { inject as service } from '@ember/service';
import { tracked } from '@glimmer/tracking';
import { set } from '@ember/object';
import $ from 'jquery';

export default class SeatingDetailsController extends Controller {
  @service AjaxUtil;
  @service session;
  @tracked details = {};
  @tracked isloading;

  initialize_details() {
    var details = {
      theatre_id: '',
      theatre_name: '',
      screen_id: '',
      user_id: this.session.data.authenticated.auth_response.user_id,
      screen_id: '',
      screen_name: '',
      located_at: '',
      platinum: { seats: [], rows: '', cols: '' },
      diamond: { seats: [], rows: '', cols: '' },
      gold: { seats: [], rows: '', cols: '' },
      silver: { seats: [], rows: '', cols: '' },
    };

    this.set('details', details);
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
  getTheatreDetails() {
    var params = {
      theatre_id: this.details.theatre_id,
      user_id: this.details.user_id,
    };
    this.AjaxUtil.ajax('api/json/action/getTheatreDetails', {
      params: JSON.stringify(params),
    }).then((jsonData) => {
      if (jsonData.status === 'success') {
        var details = {
          theatre_id: jsonData.theatre_id,
          type: this.details.type,
          theatre_name: jsonData.theatre_name,
          located_at: jsonData.located_at,
          status: jsonData.status,
          screen_id: '',
          user_id: this.details.user_id,
          screen_id: '',
          screen_name: '',
          platinum: { seats: [], rows: '', cols: '' },
          diamond: { seats: [], rows: '', cols: '' },
          gold: { seats: [], rows: '', cols: '' },
          silver: { seats: [], rows: '', cols: '' },
        };

        this.set('details', details);
      } else {
        alert('Theatre Details Not Found!!..');
        window.close();
      }
    });
  }

  @action
  getScreenDetails() {
    var params = {
      screen_id: this.details.screen_id,
      user_id: this.details.user_id,
    };
    this.AjaxUtil.ajax('api/json/action/getScreenDetails', {
      params: JSON.stringify(params),
    }).then((jsonData) => {
      if (jsonData.status == 'success') {
        var screen_details = {
          screen_id: this.details.screen_id,
          type: this.details.type,
          user_id: this.details.user_id,
          screen_name: jsonData.screen_name,
          theatre_name: jsonData.theatre_name,
          located_at: jsonData.located_at,
          status: jsonData.status,
          diamond: {
            seats: jsonData.diamond_seats,
            rows: parseInt(jsonData.diamond_dimension.split('/')[0], 10) || 0,
            cols: parseInt(jsonData.diamond_dimension.split('/')[1], 10) || 0,
          },
          platinum: {
            seats: jsonData.platinum_seats,
            rows: parseInt(jsonData.platinum_dimension.split('/')[0], 10) || 0,
            cols: parseInt(jsonData.platinum_dimension.split('/')[1], 10) || 0,
          },
          gold: {
            seats: jsonData.gold_seats,
            rows: parseInt(jsonData.gold_dimension.split('/')[0], 10) || 0,
            cols: parseInt(jsonData.gold_dimension.split('/')[1], 10) || 0,
          },
          silver: {
            seats: jsonData.silver_seats,
            rows: parseInt(jsonData.silver_dimension.split('/')[0], 10) || 0,
            cols: parseInt(jsonData.silver_dimension.split('/')[1], 10) || 0,
          },
        };
        this.set('details', screen_details);
        this.applyDimensions();
      } else {
        alert('Screen Details Not Found!!!..');
        window.close();
      }
    });
  }

  @action
  updateDetails(event) {
    event.preventDefault();
    var details = this.details;
    var params = {
      screen_id: details.screen_id,
      theatre_id: details.theatre_id,
      user_id: details.user_id,
      screen_name: $('#screen_name').val(),
      platinum_seats: details.platinum.seats,
      platinum_dimension: details.platinum.rows + '/' + details.platinum.cols,
      diamond_seats: details.diamond.seats,
      diamond_dimension: details.diamond.rows + '/' + details.diamond.cols,
      gold_seats: details.gold.seats,
      gold_dimension: details.gold.rows + '/' + details.gold.cols,
      silver_seats: details.silver.seats,
      silver_dimension: details.silver.rows + '/' + details.silver.cols,
    };

    var url =
      this.details.type == 'addScreen'
        ? 'api/json/action/addNewScreen'
        : 'api/json/action/updateTheatreDetails';
    this.isloading = 'true';
    this.AjaxUtil.ajax(url, {
      params: JSON.stringify(params),
    }).then((jsonData) => {
      this.isloading = 'false';
      this.updateStatusMessage(jsonData);
      if (jsonData.status == 'success') {
        alert(jsonData.message);
        window.close();
      }
    });
  }

  @action
  deleteScreen() {
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
        this.AjaxUtil.ajax('api/json/action/deleteScreen', {
          screen_id: this.details.screen_id,
        }).then((jsonData) => {
          this.isloading = 'false';
          this.updateStatusMessage(jsonData);
          if (jsonData.status == 'success') {
            alert(jsonData.message);
            window.close();
          }
        });
      }
    });
  }

  @action
  applyDimensions() {
    this.seatArrangements('diamond');
    this.seatArrangements('platinum');
    this.seatArrangements('gold');
    this.seatArrangements('silver');
  }

  @action
  seatArrangements(id) {
    var seats = [];
    var rows = this.details[id].rows;
    var cols = this.details[id].cols;
    var exSeats = this.details[id].seats;
    this.deleteAllRows(id + '_layout');
    var { seat_id, index } = this.generateSequence(id + '_layout');
    let layout = document.getElementById(id + '_layout');
    for (let i = 0; i < rows; i++) {
      var row = layout.insertRow(i);
      index = index + 1;
      for (let j = 0; j <= cols; j++) {
        let seat = row.insertCell(j);
        if (j == 0) {
          seat.innerHTML = seat_id[index];
          seat.id = 'row';
        } else {
          seat.innerHTML = j;
          seat.id = seat_id[index] + j;
          if (exSeats.includes(seat.id)) {
            document.getElementById(seat.id).classList.add('select-ticket');
            seats.push(seat.id);
          }
        }
      }
    }
    this.set('details.' + id + '.seats', seats);
  }

  @action
  toggleSeats(id, event) {
    var seats = this.details[id].seats;
    var seat = event.target.id;
    if (
      event.target.tagName === 'TD' &&
      event.target.id != 'row' &&
      event.target.id != 'empty-row'
    ) {
      this.set('details.' + id + '.seats', []);
      if (event.target.classList.contains('select-ticket')) {
        event.target.classList.remove('select-ticket');
        seats = seats.filter((item) => item !== seat);
      } else {
        event.target.classList.add('select-ticket');
        seats.push(seat);
      }
      this.set('details.' + id + '.seats', seats);
    } else if (event.target.id == 'row') {
      this.set('details.' + id + '.seats', []);
      if (event.target.classList.contains('selected-row')) {
        for (var i = 1; i <= this.details[id].cols; i++) {
          document
            .getElementById(event.target.textContent + i)
            .classList.remove('select-ticket');
          var ele = event.target.textContent + i;
          seats = seats.filter((item) => item !== ele);
        }
        event.target.classList.remove('selected-row');
      } else {
        for (var i = 1; i <= this.details[id].cols; i++) {
          if (seats.indexOf(event.target.textContent + i) == -1) {
            document
              .getElementById(event.target.textContent + i)
              .classList.add('select-ticket');
            seats.push(event.target.textContent + i);
          }
        }
        event.target.classList.add('selected-row');
      }
      this.set('details.' + id + '.seats', seats);
    }
  }

  @action
  generateSequence(id) {
    const result = [];
    const uppercaseLetters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
    const lowercaseLetters = 'abcdefghijklmnopqrstuvwxyz';

    for (let i = 0; i < uppercaseLetters.length; i++) {
      result.push(uppercaseLetters[i]);
    }

    for (let i = 0; i < uppercaseLetters.length; i++) {
      for (let j = 0; j < lowercaseLetters.length; j++) {
        result.push(uppercaseLetters[i] + lowercaseLetters[j]);
      }
    }

    var index = 0;

    if (id == 'platinum_layout') {
      index = this.details.diamond.rows;
    } else if (id == 'gold_layout') {
      index = this.details.platinum.rows + this.details.diamond.rows;
    } else if (id == 'silver_layout') {
      index =
        this.details.platinum.rows +
        this.details.diamond.rows +
        this.details.gold.rows;
    }

    var detials = { seat_id: result, index: index - 1 };
    return detials;
  }

  @action
  changeDimensions(id) {
    this.details[id].rows = parseInt($('#' + id + '_rows').val(), 10);
    this.details[id].cols = parseInt($('#' + id + '_cols').val(), 10);
    this.applyDimensions();
  }

  @action
  deleteAllRows(id) {
    var table = document.getElementById(id);
    var tbody = table.querySelector('tbody');
    if (tbody) {
      table.removeChild(tbody);
    }
  }

  @action
  closeWindow() {
    window.close();
  }

  @action
  submitForm() {
    document.getElementById('submit-btn-form').click();
  }
}
