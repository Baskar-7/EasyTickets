import Route from '@ember/routing/route';

export default class TrailerRoute extends Route {
  setupController(controller, model) {
    controller.initializeDetails(model.movie_name);
    controller.getTrailers();
  }

  model(movie_name) {
    return movie_name;
  }
}
