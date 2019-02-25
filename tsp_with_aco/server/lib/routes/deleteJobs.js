'use strict';
const fs = require('fs');
const deleteJobs = function (database) {
  if (!database) {
    throw new Error('Database is missing.');
  }
  return function (req, res)  {

  fs.unlink("./tspjobs/" + req.params.id + ".json", function(error) {
        if (error) {
            return res.status(500).send(req.params.id);
            deleteJobsDb(req, res, 1);
        } else {
            console.log('Deleted ' + req.params.id + '.json');
            deleteJobsDb(req, res, 0);
        }
    });
  };

  function deleteJobsDb(req,res,uLinkErr) {
    database.deleteJobs(req.params.id, (err) => {
      if (err) {
        if(uLinkErr==0)
        return res.status(500).end();
      }
      if(uLinkErr==0)
      return res.status(201).send(req.params.id);
    });
  }


};

module.exports = deleteJobs;
