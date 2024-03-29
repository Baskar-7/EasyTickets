import Controller from '@ember/controller';
import { action } from '@ember/object';
import { inject as service } from '@ember/service';
import { tracked } from '@glimmer/tracking';

export default class TrailerController extends Controller {
  @service AjaxUtil; //makes any http request to the server where it is a service 
  @tracked trailer_details;
  @tracked isloading; //creates a loading effect to the user using a component <-- Loader -->

  @action
  initializeDetails(movie_name) {
    var trailer_details = {
      movie_name: movie_name,
      trailers: {},
    };

    this.set('trailer_details', trailer_details);
  }

  @action
  getTrailers() {
    var apiKey = 'AIzaSyBYDJVOcufxFgrTPejjTQOYo6SUrbO7ajE';

    var url = `https://www.googleapis.com/youtube/v3/search?part=snippet&q=${this.trailer_details.movie_name} official trailer&type=trailer&maxResults=5&key=${apiKey}`;

    this.isloading='true';
    this.AjaxUtil.getTrailers(url).then((jsonData) => {
      this.isloading='false'
      var trailer_details = {
        movie_name: this.trailer_details.movie_name,
        trailers: jsonData.items,
      };

      this.set('trailer_details', trailer_details);
    });
  }

}
