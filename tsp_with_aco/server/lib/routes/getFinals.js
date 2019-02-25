'use strict';

const getFinals = function (database) {
  if (!database) {
    throw new Error('Database is missing.');
  }

  return function (req, res)  {
    database.getFinals((err, mappings) => {
      if (err) {
        return res.status(500).end();
      }
      res.send(mappings);
    })
  };
};

module.exports = getFinals;
