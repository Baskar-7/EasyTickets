import Component from '@ember/component';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';
import { htmlSafe } from '@ember/template';
import { set } from '@ember/object';
import $ from 'jquery';

export default class TableComponent extends Component {
  @tracked cookieData = {};

  get isVisible() {
    if (this.layout.actions.length === 1) {
      return htmlSafe('visibility: hidden;');
    }
  }

  @action
  shouldDisplayAction(actionName, user) {
    var actionsFor = this.layout.actionsFor;
    if (actionsFor && actionsFor[actionName]) {
      var pair = actionsFor[actionName];
      var key = Object.keys(pair)[0];
      if (user[key] == pair[key]) return true;
      else return false;
    } else if (this.layout.actions.includes(actionName)) {
      return true;
    } else {
      return false;
    }
  }

  @action
  toggleDropdown(event) {
    var dropdown = $('.dropdown');
    if (event.type == 'focusout') {
      dropdown.removeClass('active');
      dropdown.find('.dropdown-menu').slideUp(300);
    } else {
      dropdown.toggleClass('active');
      dropdown.find('.dropdown-menu').slideToggle(300);
    }
  }

  @action
  filterBy(event) {
    var filterValue = event.target.id;
    $('.' + this.layout.label + '_selected').text(filterValue);
    this.toggleDropdown('focusout');
    set(this.layout, 'filterValue', filterValue);
    this.action('refresh', '');
  }

  @action
  sortBy(columnName) {
    if (this.layout.sortArray.includes(columnName)) {
      this.set('layout.sortingOrder', !this.get('layout.sortingOrder'));
    } else {
      this.set('layout.sortingOrder', true);
    }
    this.set('layout.sortCol', columnName);
    this.action('refresh', '');
  }

  @action
  search(event) {
    if (event.keyCode == 13) {
      var searchText = event.target.value;
      set(this.layout, 'search', searchText);
      this.action('refresh', '');
    }
  }

  @action
  select(event) {
    if (this.layout.actions.length == 1) {
      const clickedRow = event.currentTarget;
      if (clickedRow) {
        const element = clickedRow.querySelector('td:first-child');
        var lastElement = this.cookieData.lastElement;

        if (element.style.visibility === 'visible')
          element.style.visibility = 'hidden';
        else {
          if (lastElement) lastElement.style.visibility = 'hidden';
          element.style.visibility = 'visible';
          this.cookieData.lastElement = element;
        }
      }
    }
  }

  @action
  handleActions(action, param, event) {
    event.stopPropagation();
    this.action(action, param);
  }
}
