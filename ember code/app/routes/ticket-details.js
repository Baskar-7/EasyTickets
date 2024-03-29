import Route from '@ember/routing/route';
import { inject as service } from '@ember/service';
import { action } from '@ember/object';

export default class IndexRoute extends Route {
  @service store;

  @action
  setupController(controller, model) {
    var param = model.id;
    var params = param.split('&');
    controller.init();
    if (params.length == 2) {
      controller.set('ticket_details.block_id', params[0]);
      controller.set('ticket_details.history_id', params[1]);
      controller.getTicketDetails();
    } else {
      window.location.href = '';
      window.location.href = '/access-error';
    }
  }

  async model(id) {
    // const movie = {
    //   id: '1',
    //   Mname: 'Ponniyin Selvan',
    //   Tname: 'AGS Cinemas',
    //   No_of_rows: '11',
    //   No_of_cols: '20',
    //   price: '250',
    //   location: 'Maduravoyal',
    //   time: '15 Nov, 03:25 PM',
    //   certificate: 'A',
    //   image: `\PS.jpg`,
    //   Bimage: `/PSB.jpg`,
    //   genre: 'Action/Adventure/Drama/Historical',
    // };
    // return movie;
    return id;
  }
}
