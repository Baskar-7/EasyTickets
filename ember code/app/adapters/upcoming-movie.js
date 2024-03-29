import RESTAdapter from '@ember-data/adapter/rest';

export default class UpcomingMovieAdapter extends RESTAdapter {

  buildURL(...args) {
     return 'https://api.themoviedb.org/3/movie/upcoming?api_key=06a1470f2e6479ec679ba7d2f5700f08&language=ta&region=IN&page=1';
    // return 'https://censusindia.gov.in/nada/index.php/api/tables/data/2011/geo_codes/?level=district&fields=district,areaname&limit=640';
  }

}
