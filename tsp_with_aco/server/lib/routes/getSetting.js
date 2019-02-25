'use strict';

const getSetting = function (database) {
  if (!database) {
    throw new Error('Database is missing.');
  }
  return function (req, res)  {
    database.getSetting(req.params.id, (err, mappings) => {
      if (err) {
        return res.status(500).end();
      }
      res.send(mappings);
    })
  };
};

module.exports = getSetting;
