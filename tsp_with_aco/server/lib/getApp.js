'use strict';

const express = require('express'),
      bodyParser = require('body-parser'),
      uuid = require('uuidv4');
      
const routes= require('./routes');

var path = require('path');

const getApp = function(database) {
  if (!database) {
    throw new Error('Database is missing!');
  }

  const app=express();
  app.set('view engine', 'ejs');

  app.use(express.static(path.join(__dirname, '../client')));
  app.use(bodyParser.urlencoded({     // to support URL-encoded bodies
    extended: true
  }));

  app.post('/auftrag', routes.postAuftrag(database));

  app.get('/Job/:id', routes.getJob(database));
  app.get('/setting/:id', routes.getSetting(database));
  app.get('/settings', routes.getSettings(database));
  app.get('/route/:id', routes.getRoute(database));
  app.get('/routes', routes.getRoutes(database));
  app.delete('/job/:id', routes.deleteJobs(database));
  app.get('/final/:id', routes.getFinal(database));
  app.get('/finals', routes.getFinals(database));
  app.get('/iteration/:id', routes.getIteration(database));
  app.get('/iterations', routes.getIterations(database));

  return app;
};

module.exports = getApp;
