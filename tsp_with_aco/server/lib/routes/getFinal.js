'use strict';

const getFinal = function (database) {
  if (!database) {
    throw new Error('Database is missing.');
  }
  return function (req, res)  {
    database.getFinal(req.params.id, (err, mappings) => {
      if (err) {
        return res.status(500).end();
      }
      res.send(mappings);
    })
  };
};

module.exports = getFinal;