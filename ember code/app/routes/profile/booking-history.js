import Route from '@ember/routing/route';

export default class BookingHistoryRoute extends Route {
  setupController(controller, model) {
    controller.initializeDetails();
    controller.getBookingHistories();
    controller.getShowHistories();
  }
}
