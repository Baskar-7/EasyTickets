import Controller from '@ember/controller';
import { inject as service } from '@ember/service';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';
import { set } from '@ember/object';
import $ from 'jquery';

export default class BookTicketsController extends Controller {
  @service tickets;
  @service AjaxUtil;
  @service router;
  @tracked count = 0;
  @tracked show_details = {};

  initializeDetails() {
    super.init(...arguments);
    var show_details = {
      movie_name: '',
      theatre_name: '',
      located_at: '',
      movie_certificate: '',
      movie_time: '',
      diamond: { seats: [], rows: '', cols: '', selected_seats: [] },
      platinum: { seats: [], rows: '', cols: '', selected_seats: [] },
      gold: { seats: [], rows: '', cols: '', selected_seats: [] },
      silver: { seats: [], rows: '', cols: '', selected_seats: [] },
      history_id: '',
      show_id: '',
      ticket_count: 0,
      total_price: '',
    };

    this.set('show_details', show_details);
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
  getShowDetails() {
    this.AjaxUtil.ajax('api/json/action/getShowDetails', {
      history_id: this.show_details.history_id,
    }).then((jsonData) => {
      var show_details = {
        show_id: jsonData.show_id,
        movie_name: jsonData.movie_name,
        movie_time: jsonData.show_startDate,
        movie_certificate: jsonData.movie_certificate,
        theatre_name: jsonData.theatre_name,
        located_at: jsonData.located_at,
        sold_seats: jsonData.blocked_seats,
        ticket_count: this.show_details.ticket_count,
        diamond: {
          seats: jsonData.diamond_seats,
          rows: parseInt(jsonData.diamond_dimension.split('/')[0], 10) || 0,
          cols: parseInt(jsonData.diamond_dimension.split('/')[1], 10) || 0,
          price: jsonData.diamond_price,
          selected_seats: this.show_details.diamond.selected_seats,
        },
        platinum: {
          seats: jsonData.platinum_seats,
          rows: parseInt(jsonData.platinum_dimension.split('/')[0], 10) || 0,
          cols: parseInt(jsonData.platinum_dimension.split('/')[1], 10) || 0,
          price: jsonData.platinum_price,
          selected_seats: this.show_details.platinum.selected_seats,
        },
        gold: {
          seats: jsonData.gold_seats,
          rows: parseInt(jsonData.gold_dimension.split('/')[0], 10) || 0,
          cols: parseInt(jsonData.gold_dimension.split('/')[1], 10) || 0,
          price: jsonData.gold_price,
          selected_seats: this.show_details.gold.selected_seats,
        },
        silver: {
          seats: jsonData.silver_seats,
          rows: parseInt(jsonData.silver_dimension.split('/')[0], 10) || 0,
          cols: parseInt(jsonData.silver_dimension.split('/')[1], 10) || 0,
          price: jsonData.silver_price,
          selected_seats: this.show_details.silver.selected_seats,
        },
        history_id: this.show_details.history_id,
        selected_seats: this.show_details.selected_seats,
        total_price: '',
      };
      this.set('show_details', show_details);
       console.log(this.show_details);
      this.applyDimensions();
    });
  }

  @action
  applyDimensions() {
    if (this.show_details.diamond.seats.length != 0)
      this.seatArrangements('diamond');
    if (this.show_details.platinum.seats.length != 0)
      this.seatArrangements('platinum');
    if (this.show_details.gold.seats.length != 0) this.seatArrangements('gold');
    if (this.show_details.silver.seats.length != 0)
      this.seatArrangements('silver');
  }

  @action
  seatArrangements(id) {
    var seating_type = this.show_details[id];
    var rows = seating_type.rows;
    var cols = seating_type.cols;
    var exSeats = seating_type.seats;
    var price = seating_type.price;
    var soldSeats = this.show_details.sold_seats;
    var { seat_id, index } = this.generateSequence(id + '_layout');
    let layout = document.getElementById(id + '_layout');
    for (let i = 0; i <= rows; i++) {
      var row = layout.insertRow(i);
      if (i == 0) {
        var cell = row.insertCell(0);
        cell.colSpan = cols + 1;
        cell.innerHTML =
          '<div>' +
          this.formatText(id) +
          ' - ' +
          this.formatCurrency(price) +
          '</div><hr>';
        cell.id = 'info';
      } else {
        index = index + 1;
        for (let j = 0; j <= cols; j++) {
          let seat = row.insertCell(j);//console.log(seat_id[index]+j +" "+(soldSeats.indexOf(seat_id[index] + j))+" "+soldSeats);
          if (j == 0) {
            seat.innerHTML = seat_id[index];
            seat.id = 'row';
          } else if (soldSeats.indexOf(seat_id[index] + j) !== -1 ) {
            seat.innerHTML = j;
            seat.classList = 'booked';
          } else if (exSeats.includes(seat_id[index] + j)) {
            seat.innerHTML = j;
            seat.id = seat_id[index] + j;
          } else {
            seat.id = 'empty-row';
          }
        }
      }
    }
  }

  @action
  formatCurrency(amount) {
    const rupees = Math.floor(amount);
    let paise = Math.ceil((amount * 100) % 100);
    if (paise.toString().length === 1) {
      paise = '0' + paise;
    }
    return '₹' + rupees + '.' + paise;
  }

  formatText(text) {
    return text.charAt(0).toUpperCase() + text.slice(1);
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
      index = this.show_details.diamond.rows;
    } else if (id == 'gold_layout') {
      index = this.show_details.platinum.rows + this.show_details.diamond.rows;
    } else if (id == 'silver_layout') {
      index =
        this.show_details.platinum.rows +
        this.show_details.diamond.rows +
        this.show_details.gold.rows;
    }

    var detials = { seat_id: result, index: index - 1 };
    return detials;
  }

  @action
  selectSeats(id, event) {
    var seats = this.show_details[id].selected_seats;
    var seat = event.target.id;
    if (
      event.target.tagName === 'TD' &&
      event.target.id != 'row' &&
      event.target.id != 'info' &&
      event.target.id != 'empty-row' &&
      event.target.classList != 'booked' &&
      !this.show_details.sold_seats.includes(seat)
    ) {
      this.set('show_details.' + [id] + '.selected_seats', []);
      if (event.target.classList.contains('select-ticket')) {
        event.target.classList.remove('select-ticket');
        seats = seats.filter((item) => item !== seat);
        this.set(
          'show_details.ticket_count',
          this.show_details.ticket_count - 1
        );
      } else if (this.show_details.ticket_count >= 8) {
        Swal.fire({
          icon: 'warning',
          title: 'Oops...',
          text: 'Maximum Seats Selected!',
          timer: 1800,
        });
      } else {
        event.target.classList.add('select-ticket');
        seats.push(seat);
        this.set(
          'show_details.ticket_count',
          this.show_details.ticket_count + 1
        );
      }
    }
    this.set('show_details.' + [id] + '.selected_seats', seats);
    this.updatePriceDetails();
  }

  @action
  confirmTickets() {
    var params = {
      diamond: this.show_details.diamond.selected_seats,
      platinum: this.show_details.platinum.selected_seats,
      gold: this.show_details.gold.selected_seats,
      silver: this.show_details.silver.selected_seats,
      show_id: this.show_details.history_id,
    };

    this.AjaxUtil.ajax('api/json/action/blockTickets', {
      params: JSON.stringify(params),
    }).then((jsonData) => {
      if (jsonData.status == 'success') {
        this.router.transitionTo(
          'ticket-details',
          jsonData.blocking_id + '&' + this.show_details.history_id
        );
      } else {
        this.updateStatusMessage(jsonData);
      }
    });
  }

  @action
  clearAll() {
    if (this.show_details.diamond.selected_seats.length != 0)
      this.clearTickets('diamond');
    if (this.show_details.platinum.selected_seats.length != 0)
      this.clearTickets('platinum');
    if (this.show_details.gold.selected_seats.length != 0)
      this.clearTickets('gold');
    if (this.show_details.silver.selected_seats.length != 0)
      this.clearTickets('silver');
    this.set('show_details.ticket_count', 0);
  }

  @action
  clearTickets(id) {
    var seats = this.show_details[id].selected_seats;
    seats.forEach((seat) => {
      document.getElementById(seat).classList.remove('select-ticket');
    });
    this.set('show_details.' + id + '.selected_seats', []);
  }

  @action
  updatePriceDetails() {
    var details = this.show_details;
    var total =
      details.diamond.selected_seats.length * details.diamond.price +
      details.platinum.selected_seats.length * details.platinum.price +
      details.gold.selected_seats.length * details.gold.price +
      details.silver.selected_seats.length * details.silver.price;
    this.set('show_details.total_price', total);
  }
}
