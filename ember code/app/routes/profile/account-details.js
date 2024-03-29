import Route from '@ember/routing/route';

export default class AccountDetailsRoute extends Route {
  setupController(controller, model) {
    controller.getProfileDetails();
  }
}
