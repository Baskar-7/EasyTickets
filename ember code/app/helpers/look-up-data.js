import { helper } from '@ember/component/helper';

export default helper(function lookUpData(params /*, named*/) {
  var operation = params[0];

  if (operation == 'getGenres') {
    var genres = {
      Action: 28,
      Adventure: 12,
      Animation: 16,
      Comedy: 35,
      Crime: 80,
      Documentary: 99,
      Drama: 18,
      Family: 10751,
      Fantasy: 14,
      History: 36,
      Horror: 27,
      Music: 10402,
      Mystery: 9648,
      Romance: 10749,
      'Sci-fi': 878,
      Thriller: 53,
      War: 10752,
      Western: 37,
      Biography: 1025,
      Sport: 1026,
      Musical: 1027,
      'Film Noir': 1028,
      Supernatural: 1029,
      Disaster: 1030,
      Mockumentary: 1031,
    };

    if (params[1] === 'All') {
      return Object.keys(genres);
    } else {
      return genres[params[1]];
    }
  } else if (operation === 'getLang') {
    const languageCodes = {
      en: 'English',
      es: 'Spanish',
      fr: 'French',
      de: 'German',
      it: 'Italian',
      pt: 'Portuguese',
      ru: 'Russian',
      zh: 'Chinese',
      ja: 'Japanese',
      ko: 'Korean',
      ar: 'Arabic',
      hi: 'Hindi',
      bn: 'Bengali',
      ur: 'Urdu',
      ta: 'Tamil',
      te: 'Telugu',
      gu: 'Gujarati',
      kn: 'Kannada',
      ml: 'Malayalam',
      pa: 'Punjabi',
      fa: 'Persian',
      tr: 'Turkish',
      th: 'Thai',
      vi: 'Vietnamese',
      nl: 'Dutch',
      sv: 'Swedish',
      da: 'Danish',
      no: 'Norwegian',
      fi: 'Finnish',
      el: 'Greek',
      pl: 'Polish',
      hu: 'Hungarian',
      cs: 'Czech',
      sk: 'Slovak',
      ro: 'Romanian',
      uk: 'Ukrainian',
      he: 'Hebrew',
      id: 'Indonesian',
      ms: 'Malay',
      fil: 'Filipino',
      sw: 'Swahili',
      af: 'Afrikaans',
      is: 'Icelandic',
      et: 'Estonian',
      lv: 'Latvian',
      lt: 'Lithuanian',
      hr: 'Croatian',
      sr: 'Serbian',
      sl: 'Slovenian',
      bg: 'Bulgarian',
      mk: 'Macedonian',
      sq: 'Albanian',
      hy: 'Armenian',
      ka: 'Georgian',
      uz: 'Uzbek',
      kk: 'Kazakh',
      ky: 'Kyrgyz',
      tg: 'Tajik',
      tk: 'Turkmen',
      mn: 'Mongolian',
      cy: 'Welsh',
      ga: 'Irish',
      gd: 'Scottish Gaelic',
      mi: 'Māori',
    };
    if (params[1] === 'All') {
      return Object.values(languageCodes);
    } else if (params[1] === 'getKeyValue') {
      return Object.keys(languageCodes).find(
        (key) => languageCodes[key] === params[2]
      );
    } else {
      return languageCodes[params[1]];
    }
  }
});
