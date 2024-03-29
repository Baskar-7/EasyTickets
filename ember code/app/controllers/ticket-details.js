import Controller from '@ember/controller';
import { inject as service } from '@ember/service';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';
import { set } from '@ember/object';
import $ from 'jquery';

export default class TicketDetailsController extends Controller {
  @service AjaxUtil;
  @service router;
  @service session;
  @tracked ticket_details = {};
  @tracked showConvenienceFees = false;
  @tracked isloading;

  init() {
    super.init(...arguments);
    var ticket_details = {
      movie_name: '',
      screen_name: '',
      theatre_name: '',
      located_at: '',
      movie_certificate: '',
      movie_time: '',
      language: '',
      projection: '',
      diamond_tickets: [],
      platinum_tickets: [],
      gold_tickets: [],
      silver_tickets: [],
      history_id: '',
      block_id: '',
    };

    this.set('ticket_details', ticket_details);
  }

  @action //toggles the container based on the choice with className
  toggleContainer(className,slideClass) {
    var element = $('.' + className);
  
      element.toggleClass('activate');
      element.toggleClass(slideClass);
    
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
  getTicketDetails() {
    var params = {
      history_id: this.ticket_details.history_id,
      block_id: this.ticket_details.block_id,
    };

    this.isloading = 'true';
    this.AjaxUtil.ajax('api/json/action/getTicketDetails', {
      params: JSON.stringify(params),
    }).then((jsonData) => {
      this.isloading = 'false';
      if (jsonData.status == 'success') {
        var ticket_details = {
          movie_name: jsonData.movie_name,
          screen_name: jsonData.screen_name,
          theatre_name: jsonData.theatre_name,
          language: jsonData.language,
          projection: jsonData.projection,
          located_at: jsonData.located_at,
          movie_certificate: jsonData.movie_certificate,
          movie_time: jsonData.show_startDate,
          diamond_tickets: {
            seats: jsonData.diamond_seats,
            price: jsonData.diamond_price,
          },
          platinum_tickets: {
            seats: jsonData.platinum_seats,
            price: jsonData.platinum_price,
          },
          gold_tickets: {
            seats: jsonData.gold_seats,
            price: jsonData.gold_price,
          },
          silver_tickets: {
            seats: jsonData.silver_seats,
            price: jsonData.silver_price,
          },
          history_id: this.ticket_details.history_id,
          block_id: this.ticket_details.block_id,
          ticket_count:
            jsonData.diamond_seats.length +
            jsonData.platinum_seats.length +
            jsonData.gold_seats.length +
            jsonData.silver_seats.length,
          total_price: '',
        };
        this.set('ticket_details', ticket_details);
        this.updatePriceDetails();
      } else {
        this.updateStatusMessage(jsonData);
      }
    });
  }

  @action
  updatePriceDetails() {
    var details = this.ticket_details;
    var total =
      details.diamond_tickets.seats.length * details.diamond_tickets.price +
      details.platinum_tickets.seats.length * details.platinum_tickets.price +
      details.gold_tickets.seats.length * details.gold_tickets.price +
      details.silver_tickets.seats.length * details.silver_tickets.price;
    this.set('ticket_details.total_price', total);
  }

  @action
  confirmTickets() {
    if (this.session.isAuthenticated) {
      var details = this.ticket_details;
      var params = {
        history_id: details.history_id,
        block_id: details.block_id,
        movie_time: details.movie_time,
        total_price: details.total_price,
        seats: document.getElementsByClassName('seats').amt.textContent,
        user_id: this.session.data.authenticated.auth_response.user_id,
      };
      this.isloading = 'true';
      this.AjaxUtil.ajax('api/json/action/confirmTickets', {
        params: JSON.stringify(params),
      }).then((jsonData) => {
        this.isloading = 'false';
        if (jsonData.status == 'success') {
          Swal.fire('', 'Tickets Booked Successfully!', 'success');
          this.router.transitionTo('index');
        } else {
          this.updateStatusMessage(jsonData);
        }
      });
    } else {
      this.toggleContainer('login-container','slidetoTop');
      var notification = {
        status: 'info',
        message: 'Authentication Required!!!',
      };
      this.updateStatusMessage(notification);
    }
  }

  @action
  Count() {
    return this.ticket_details.ticket_count;
  }

  @action
  amt(cost) {
    return this.Count() * cost;
  }

  @action
  base_amt() {
    return 30 * this.Count();
  }

  @action
  Gst() {
    return 30 * (this.Count() / 100) * 18;
  }

  @action
  ConvenienceFees() {
    return this.Gst() + 30 * this.Count();
  }

  @action
  pay() {
    var pay = this.ConvenienceFees() + this.ticket_details.total_price;
    return pay;
  }

  @action
  showConvenienceFeeDetails() {
    $('.rotate').toggleClass('down');
    this.toggleProperty('showConvenienceFees');
  }
}
