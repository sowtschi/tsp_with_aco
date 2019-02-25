'use strict';

const getRoutes = function (database) {
  if (!database) {
    throw new Error('Database is missing.');
  }
  return function (req, res)  {
    database.getRoutes((err, mappings) => {
      if (err) {
        return res.status(500).end();
      }
      res.send(mappings);
    })
  };
};

module.exports = getRoutes;
