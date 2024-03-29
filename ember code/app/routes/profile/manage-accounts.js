import Route from '@ember/routing/route';

export default class ManageAccountsRoute extends Route {
  setupController(controller, model) {
    controller.getLayoutDetails();
  }
}
