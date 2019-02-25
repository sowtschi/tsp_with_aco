'use strict';
const fs = require('fs'),
      ejs = require('ejs');

const getJob = function (database) {
  if (!database) {
    throw new Error('Database is missing.');
  }
  return function (req, res)  {
    fs.readFile(__dirname + "/../../templates/job.ejs",{encoding:'utf-8'}, function(err,filestring){
      if(err){
        throw err;
      } else {
        database.getJob(req.params.id, (err, mappings) => {
          if (err) {
            return res.status(500).end();
          }
          var json = JSON.stringify(mappings);
          json = json.replace(/body/, "route");
          mappings=JSON.parse(json);
          
          var html = ejs.render(filestring,  mappings);
          res.setHeader('content-type', 'text/html');
          res.writeHead(200);
          res.write(html);
          res.end();
        })
      }
    });
  };
};

module.exports = getJob;
