import { helper } from '@ember/component/helper';
import { htmlSafe } from '@ember/template';
import $ from 'jquery';
import DOMPurify from 'dompurify';

export default helper(function conditions(params /*, named*/) {
  var operation = params[0];

  if (operation === '==') {
    return params[1] == params[2];
  } else if (operation === '>') {
    return Number(params[1]) > Number(params[2]);
  } else if (operation === '<') {
    return Number(params[1]) < Number(params[2]);
  } else if (operation === '>=') {
    return Number(params[1]) >= Number(params[2]);
  } else if (operation === '<=') {
    return Number(params[1]) <= Number(params[2]);
  } else if (operation === '!=') {
    if (params[1] == undefined || params[2] == undefined) return false;
    return params[1] != params[2];
  } else if (operation === '!==') {
    return params[1] !== params[2];
  } else if (operation === '|==') {
    return params[1] == params[2] || params[1] == params[3];
  } else if (operation === '/') {
    return params[1] / params[2];
  } else if(operation === "reverseArray"){
    return params[1].slice().reverse();
  } else if (operation === 'isEmptyObject') {
    return $.isEmptyObject(params[1]);
  } else if (operation === 'capitalize') {
    if (typeof params[1] !== 'string') return params[1];
    return params[1].charAt(0).toUpperCase() + params[1].slice(1);
  } else if (operation == 'formatCurrency') {
    const dollars = Math.floor(params[1]);
    let cents = Math.ceil((params[1] * 100) % 100);
    if (cents.toString().length === 1) {
      cents = '0' + cents;
    }
    return `${'₹'}${dollars}.${cents}`;
  } else if (operation === 'sanitize') {
    return DOMPurify.sanitize(params[1]);
  } else if (operation === 'concat') {
    return htmlSafe(String(params[1]) + String(params[2]));
  } else if (operation === 'arrayContains') {
    if (params[1] == undefined) return false;
    for (var i = 2; i < params.length; i++) {
      if (params[1].includes(params[i])) return true;
    }
    return false;
  } else if (operation === 'get') {
    if (params[1] !== undefined && params[2] !== undefined) {
      return params[1][params[2]];
    }
  } else if (operation === 'parseToTime') {
    var hrs = parseInt(params[1] / 60, 10),
      mins = params[1] % 60,
      time;
    if (hrs > 0) time = hrs + 'hrs ';
    if (mins > 0) time = time + mins + 'mins';

    return time;
  } else if (operation === 'isEmpty') {
    return params[1] === undefined || params[1].length === 0;
  } else if (operation === 'removeSpaces') {
    return params[1].replace(/\s+/g, '');
  } else if (params[0] === 'getWidth') {
    return $(params[1]).width();
  } else if (operation === 'getYear') {
    return new Date(params[1]).getFullYear();
  } else if (operation === 'getMinDate') {
    const givenDate = new Date(params[0]);
    const currentDate = new Date();
    const minDate =
      givenDate > currentDate
        ? givenDate
        : new Date(currentDate.setDate(currentDate.getDate() + 1));
    return minDate.toISOString().slice(0, 16);
  }else if(operation === 'isDateExpired'){
    const givenDate = new Date(params[0]);
    const currentDate = new Date();
    return givenDate > currentDate;
  } else if (operation === 'getLocalTime') {
    return new Date(params[1]).toLocaleTimeString('en-US', {
      hour: 'numeric',
      minute: 'numeric',
      hour12: true,
    });
  } else if (operation === 'getDay') {
    const dateObject = new Date(params[1]);
    const daysOfWeek = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
    return daysOfWeek[dateObject.getDay()];
  } else if (operation === 'getDate') {
    return new Date(params[1]).getDate();
  } else if (params[0] === 'dateSuffix') {
    var date = params[1];
    var d = date % 10;
    switch (d) {
      case 1:
        return date + 'ST';
      case 2:
        return date + 'ND';
      case 3:
        return date + 'RD';
      default:
        return date + 'TH';
    }
  } else if (params[0] === 'isTodayorTmrw') {
    return new Date(params[1]) === new Date()
      ? 'Today,'
      : new Date(params[1]) === new Date() + 1
      ? 'Tomorrow,'
      : '';
  } else if (operation === 'getMonth') {
    const dateObject = new Date(params[1]);
    const months = [
      'Jan',
      'Feb',
      'Mar',
      'Apr',
      'May',
      'Jun',
      'Jul',
      'Aug',
      'Sep',
      'Oct',
      'Nov',
      'Dec',
    ];
    return months[dateObject.getMonth()];
  }
});
