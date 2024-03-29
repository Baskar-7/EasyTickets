import Service from '@ember/service';
import { action } from '@ember/object';
import $ from 'jquery';
import { tracked } from '@glimmer/tracking';
import { inject as service } from '@ember/service';

export default class AjaxUtilService extends Service {
  @tracked apiKey = '06a1470f2e6479ec679ba7d2f5700f08';
  @service session;
  @service router;
  @service store;
  @tracked cookie = {};
  @service router;

  fillCSRFToken(data) {
    if (this.session.auth_response) {
      data.csrfToken = this.session.auth_response.csrfToken;
    }
  }

  @action
  ajax(url, data) {
    if (navigator.onLine) {
      var self = this;
      window.scrollTo(0, 0);
      var type = 'GET';
      if (data == undefined) {
        data = {};
      }

      // this.fillCSRFToken(data);

      if (!url.startsWith('http') || !url.startsWith('https')) {
        url = 'http://localhost:8080/EasyTickets/' + url;
        type = 'POST';
      }

      return new Promise(function (resolve) {
        $.ajax({
          url: url,
          data: data,
          async: true,
          type: type,
          dataType: 'json',
          SameSite: 'none',
          credentials: 'include',
        })
          .done(function (json, status, jqXHR) {
            // if (json.hasOwnProperty('JSESSIONID')) {
            //self.setCookie('JSESSIONID', json.JSESSIONID, 1);
            // }
            resolve(json);
          })
          .fail(function (jqXHR, textStatus, errorThrown) {
            if (jqXHR.code == '403' || jqXHR.code == '404') {
              self.router.transitionTo('access-error');
            }

            var json = {
              status: 'error',
              message: 'failed to load data!',
            };
            resolve(json);
          });
      });
    } else {
      this.router.transitionTo('networkError');
    }
  }

  @action
  createFormAndSubmitFile(url, file, data) {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('data', data);

    return new Promise(function (resolve) {
      $.ajax({
        url: 'http://localhost:8080/EasyTickets/' + url,
        type: 'POST',
        data: formData,
        contentType: false,
        processData: false,
      })
        .done(function (json, status, jqXHR) {
          resolve({ status: 'success' });
        })
        .fail(function (jqXHR, textStatus, errorThrown) {
          if (jqXHR.code == '403' || jqXHR.code == '404') {
            self.router.transitionTo('access-error');
          }

          var json = {
            status: 'error',
            message: 'failed to load data!',
          };
          resolve(json);
        });
    });
  }

  @action
  clearAllCookies() {
    var cookies = document.cookie.split(';');

    for (var i = 0; i < cookies.length; i++) {
      var cookie = cookies[i];
      var eqPos = cookie.indexOf('=');
      var name = eqPos > -1 ? cookie.substr(0, eqPos) : cookie;
      document.cookie =
        name + '=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
    }
  }

  @action
  async tmdbApi(movieTitle) {
    if (navigator.onLine) {
      const searchUrl = `https://api.themoviedb.org/3/search/movie?api_key=${
        this.apiKey
      }&query=${encodeURIComponent(movieTitle + ' ')}`;

      return new Promise(function (resolve) {
        fetch(searchUrl)
          .then((response) => response.json())
          .then((data) => {
            if (data.results && data.results.length > 0) {
              const sortedMovies = data.results.sort((a, b) => {
                return new Date(b.release_date) - new Date(a.release_date);
              });

              resolve(sortedMovies);
            } else {
              var json = {
                status: 'info',
                message: 'Movie Details not found!!..',
              };
              resolve(json);
            }
          })
          .catch((error) => {
            console.error('Error searching for the movie:', error);
          });
      });
    } else {
      var json = {
        status: 'info',
        message: 'No Internet Connection',
      };
      return json;
    }
  }

  @action
  async getLocations() {
    const store = this.store;
    const existingRecords = store.peekAll('locations');

    if (existingRecords.length > 0) {
      return existingRecords;
    }

    var results = [],cities=[];

    await this.ajax(
      'https://censusindia.gov.in/nada/index.php/api/tables/data/2011/geo_codes/?level=district&fields=district,areaname&limit=640'
    ).then((jsonData) => {
      results = jsonData.data;
    });

    if (results && results.length > 1) {
      results.sort((a, b) => a.areaname.localeCompare(b.areaname))
       cities=results.map((location) => {
        let data = store.createRecord('locations', {
          id: location.district,
          district: location.areaname,
        });
        return data;
      });
    }
    return cities;
  }

  @action
  async getUpcomingMovies() {
    const store = this.store;
    const existingRecords = store.peekAll('upcoming-movie');

    if (existingRecords.length > 0) {
      return existingRecords;
    }

    let currentPage = 1;
    let totalPages = 1;
    let allResults = [];

    while (currentPage <= totalPages) {
      const pageData = await this.fetchPage(currentPage);
      allResults = allResults.concat(pageData.results);
      totalPages = pageData.total_pages;
      currentPage++;
    }
    var movies = [];

    if (allResults.length > 1) {
      movies = allResults.map((movie) => {
        let data = store.createRecord('upcoming-movie', {
          id: movie.id,
          backdrop_path: movie.backdrop_path,
          original_language: movie.original_language,
          original_title: movie.original_title,
          title: movie.title,
          overview: movie.overview,
          popularity: movie.popularity,
          poster_path: movie.poster_path,
          release_date: movie.release_date,
          video: movie.video,
          genre_ids: movie.genre_ids,
          vote_average: movie.vote_average,
          vote_count: movie.vote_count,
        });
        return data;
      });
    }

    return movies;
  }

  @action
  async fetchPage(currentPage) {
    try {
      const url = `https://api.themoviedb.org/3/movie/upcoming?api_key=${this.apiKey}&language=ta&region=IN&page=${currentPage}`;
      const response = await fetch(url);
      return await response.json();
    } catch (err) {
      console.log(err);
    }
    return {};
  }

  async omdbAPI(imdbId, data) {
    var omdbApiKey = '11c9f3ca';
    const omdbUrl = `https://www.omdbapi.com/?i=${imdbId}&apikey=${omdbApiKey}`;

    try {
      const response = await fetch(omdbUrl);
      if (!response.ok) {
        throw new Error('Error fetching IMDb ratings');
      }
      const omdbData = await response.json();
      if (omdbData.Language) {
        data.Language = omdbData.Language.split(',');
      }
      data.Genre = omdbData.Genre = omdbData.Genre.split(',')
        .map((genre) => genre.trim())
        .map((genre) => genre.charAt(0).toUpperCase() + genre.slice(1));
    } catch (error) {
      console.error('Error fetching IMDb rating:', error);
      throw error;
    }
  }

  async getMovieDetails(movieId) {
    if (navigator.onLine) {
      var api_key = this.apiKey,
        self = this;
      const movieUrl = `https://api.themoviedb.org/3/movie/${movieId}?api_key=${api_key}&append_to_response=release_dates,credits`;

      return new Promise(function (resolve) {
        fetch(movieUrl)
          .then((response) => response.json())
          .then(async (data) => {
            var movie_details = {};
            const selectedCrewMembers = data.credits.crew.filter(
              (crewMember) =>
                crewMember.job === 'Music' ||
                crewMember.job === 'Original Music Composer' ||
                crewMember.job === 'Director'
            );

            const cast = data.credits.cast.filter(
              (castMember) => castMember.known_for_department === 'Acting'
            );

            data.cast = [...selectedCrewMembers, ...cast];

            data.cast.forEach((crewMember) => {
              crewMember.known_for_department = crewMember.known_for_department
                .replace('Acting', 'Actor')
                .replace('Sound', 'Music')
                .replace('Directing', 'Director')
                .replace('Writing', 'Director');
            });

            if (data.hasOwnProperty('release_dates')) {
              const certification = data.release_dates.results.find(
                (result) => {
                  return result.iso_3166_1 === 'IN';
                }
              );
              if (certification) {
                data.certification =
                  certification.release_dates[0].certification;
              }
            }
            await self.omdbAPI(data.imdb_id, data),
              (movie_details = {
                languages: data.Language,
                certification: data.certification,
                crew: data.credits.crew,
                cast: data.cast,
                genres: data.Genre,
                imdb_id: data.imdb_id,
                overview: data.overview,
                poster_path: data.poster_path,
                backdrop_path: data.backdrop_path,
                release_date: data.release_date,
                runtime: data.runtime.toString(),
                title: data.title,
                vote_average: data.vote_average,
              });

            resolve(movie_details);
          })
          .catch((error) => {
            console.error('Error fetching movie details:', error);
          });
      });
    } else {
      var json = {
        status: 'info',
        message: 'No Internet Connection',
      };
      return json;
    }
  }

  async getTrailers(searchUrl) {
    return new Promise(function (resolve) {
      fetch(searchUrl)
        .then((response) => response.json())
        .then((data) => {
          resolve(data);
        })
        .catch((error) => {
          console.error('Error while searching trailer:', error);
        });
    });
  }

  addCookie(name, value, daysToExpire) {
    this.removeCookie(name);
    var date = new Date();
    date.setTime(date.getTime() + daysToExpire * 24 * 60 * 60 * 1000);
    var expires = 'expires=' + date.toUTCString();
    document.cookie = name + '=' + value + ';' + expires + ';path=/';
  }

  getCookie(cookieName) {
    cookieName = cookieName + '=';
    const decodedCookie = decodeURIComponent(document.cookie);
    const cookieArray = decodedCookie.split(';');

    for (const cookie of cookieArray) {
      let trimmedCookie = cookie.trim();
      if (trimmedCookie.indexOf(cookieName) === 0) {
        return trimmedCookie.substring(cookieName.length);
      }
    }

    return null;
  }

  removeCookie(name) {
    document.cookie =
      name + '=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
  }
}
