import Model, { attr } from '@ember-data/model';

export default class UpcomingMovieModel extends Model {
  @attr('string') backdrop_path;
  @attr('string') original_language;
  @attr('string') original_title;
  @attr('array') genre_ids;
  @attr('string') overview;
  @attr('number') popularity;
  @attr('string') poster_path;
  @attr('number') release_date;
  @attr('string') title;
  @attr('boolean') video;
  @attr('number') vote_average;
  @attr('number') vote_count;
}
