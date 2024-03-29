import Controller from '@ember/controller';
import { action } from '@ember/object';
import { inject as service } from '@ember/service';
import { tracked } from '@glimmer/tracking';
import $ from 'jquery';

export default class MovieController extends Controller {
  @service AjaxUtil;
  @tracked show_details = {};
  @tracked isloading;
  @tracked statusMessage = {};

  //api to get video key https://api.themoviedb.org/3/movie/tt7892066/videos?api_key=06a1470f2e6479ec679ba7d2f5700f08
  //trailer link "https://www.youtube.com/watch?v="+key

  @action
  initialize() {
    var show_details = {
      movie_name: '',
      movie_overview: '',
      movie_certificate: '',
      movie_posterpath: '',
      movie_genres: '',
      movie_runtime: '',
      movie_releaseDate: '',
      movie_casts: '',
      show_dates: '',
      shows: '',
      show_date: '',
      show_id: '',
    };
    this.set('show_details', show_details);
  }

  @action
  getMovieDetails() {
    var params = {
      show_date: this.show_details.show_date,
      show_id: this.show_details.show_id,
      showTimeFilter: this.show_details.showTimeFilter || [],
    };

    this.AjaxUtil.ajax('api/json/action/getMovieDetails', {
      params: JSON.stringify(params),
    }).then((jsonData) => {
      var show_details = {
        movie_name: jsonData.movie_details.movie_name,
        movie_overview: jsonData.movie_details.movie_overview,
        movie_certificate: jsonData.movie_details.movie_certificate,
        movie_posterpath: jsonData.movie_details.poster_path,
        movie_genres: jsonData.movie_details.movie_genres,
        movie_runtime: jsonData.movie_details.movie_runtime,
        movie_releaseDate: jsonData.movie_details.movie_releaseDate,
        movie_casts: jsonData.movie_details.cast_details,
        show_dates: jsonData.show_dates,
        shows: jsonData.show_details,
        show_date: this.show_details.show_date || jsonData.show_dates[0],
        showTimeFilter: this.show_details.showTimeFilter || [],
        show_id: this.show_details.show_id,
      };
      this.set('show_details', show_details);
      this.toggleScrollIcons();
    });
  }

  @action
  toggleDropdown(className, option) {
    var dropdown = $('.' + className);
    if (option == 'close') {
      dropdown.removeClass('active');
      dropdown.find('.dropdown-menu').slideUp(300);
    } else {
      dropdown.toggleClass('active');
      dropdown.find('.dropdown-menu').slideToggle(300);
    }
  }

  @action
  filterShowDate(date) {
    this.set('show_details.show_date', date);
    this.getMovieDetails();
  }

  @action
  filterTiming(event) {
    var timing = event.target.value;
    var timefilter = this.show_details.showTimeFilter;
    if (timefilter.includes(timing))
      timefilter = timefilter.filter((item) => item !== timing);
    else timefilter.push(timing);

    this.set('show_details.showTimeFilter', timefilter);
    this.getMovieDetails();
  }

  @action
  scrollBy(direction, id) {
    const dateElement = document.querySelector('#' + id);
    const scrollAmount = direction === 'forward' ? 200 : -200;
    dateElement.scrollBy({
      left: scrollAmount,
      behavior: 'smooth',
    });
  }

  toggleScrollIcons() {
    const dateElement = document.querySelector('#date');
    const leftScrollIcon = document.querySelector('.left-scroll-icon');
    const rightScrollIcon = document.querySelector('.right-scroll-icon');
    leftScrollIcon.style.visibility =
      dateElement.scrollLeft > 0 ? 'visible' : 'hidden';
    rightScrollIcon.style.visibility =
      dateElement.scrollLeft < dateElement.scrollWidth - dateElement.clientWidth
        ? 'visible'
        : 'hidden';
  }

  @action
  printConsole(param) {
    console.log(param);
  }

  @action
  watchTrailer(movie_name) {
    const url = 'http://localhost:4200/watch-trailer/' + movie_name;
    window.open(
      url,
      '_blank',
      `width=700,height=700,left=${(window.screen.width - 700) / 2},top=${
        (window.screen.height - 700) / 2
      }`
    );
  }
}
