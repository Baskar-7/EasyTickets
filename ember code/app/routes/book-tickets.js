import Route from '@ember/routing/route';
import { inject as service } from '@ember/service';

export default class TicketDetailsRoute extends Route {
  @service router;

  beforeModel(transistion) {
    if (!navigator.onLine) {
      this.session.set('attemptedTransition', transistion);
      this.router.transitionTo('networkError');
    }
  }

  setupController(controller, model) {
    controller.initializeDetails();
    controller.set('show_details.history_id', model.history_id);
    controller.getShowDetails();
  }

  model(history_id) {
    return history_id;
  }
}
