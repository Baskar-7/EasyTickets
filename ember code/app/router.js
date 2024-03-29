import EmberRouter from '@ember/routing/router';
import config from 'movie-ticket-booking/config/environment';

export default class Router extends EmberRouter {
  location = config.locationType;
  rootURL = config.rootURL;
}

Router.map(function () {
  this.route('index', { path: '/home' });
  this.route('book-tickets', { path: '/book-tickets/:history_id' });
  this.route('ticket-details', { path: '/ticket-details/:id' });
  this.route('shows', { path: '/shows/:show_id' });

  this.route('profile', function () {
    this.route('booking-history');
    this.route('account-details');
    this.route('add-show');
    this.route('manage-accounts');
  });
  this.route('networkError');
  this.route('seating-details', { path: 'seating-details/:screen_id' });
  this.route('access-error', { path: '/*path' });
  this.route('trailer', { path: '/watch-trailer/:movie_name' });
});
