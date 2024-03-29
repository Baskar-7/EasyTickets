import Route from '@ember/routing/route';

export default class AddShowRoute extends Route {
  setupController(controller, model) {
    controller.initializeDetails();
  }
}
