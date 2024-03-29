import Route from '@ember/routing/route';
import { inject as service } from '@ember/service';

export default class IndexRoute extends Route {
  @service router;
  @service session;
  @service store;
  @service AjaxUtil;

  async beforeModel(transistion) {
    if (!navigator.onLine) {
      this.session.set('attemptedTransition', transistion);
      this.router.transitionTo('networkError');
    }
  }

  async model() {
    return this.AjaxUtil.getUpcomingMovies();
  }

  setupController(controller, model) {
    controller.initialize(model);
    controller.getShows();
  }

  resetController(controller, isExiting) {
    if (isExiting) {
      controller.resetProperties();
    }
  }
}
