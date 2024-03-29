import Route from '@ember/routing/route';
import { inject as service } from '@ember/service';

export default class ShowsRoute extends Route {
  @service router;

  beforeModel(transistion) {
    if (!navigator.onLine) {
      this.session.set('attemptedTransition', transistion);
      this.router.transitionTo('networkError');
    }
  }

  setupController(controller, model) {
    controller.initialize();
    controller.set('show_details.show_id', model.show_id);
    controller.getMovieDetails();
  }

  model(show_id) {
    return show_id;
  }
}
