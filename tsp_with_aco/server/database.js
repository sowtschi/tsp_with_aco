'use strict';

const mongo = require('mongodb'),
      uuid = require('uuidv4');

const MongoClient = mongo.MongoClient;

const database = {
  initialize(connectionString, callback) {
    MongoClient.connect(connectionString, { autoReconnect: true}, (err, database) => {
      if (err) {
        return callback(err);
      }
      if (!connectionString) {
        throw new Error ('connectionString is missing.');
      }
      if (!callback) {
        throw new Error ('Callback is missing.');
      }

      const mappings=database.collection('mappings');

      this.mappings = mappings;
      callback(null);
    });
  },
  createFinal(body, callback) {
    if (!body) {
      throw new Error ('body is missing.');
    }
    if (!callback) {
      throw new Error ('Callback is missing.');
    }

    const mapping = {
      body: body,
      type: "F"
    };
    this.mappings.insertOne(mapping, err => {
      if (err) {
        return callback(err);
      }
      callback(null);
    });
  },

  getFinals(callback) {
    if (!callback) {
      throw new Error ('Callback is missing.');
    }
    this.mappings.find({type: "F"}).toArray((err,mappings) => {
      if (err) {
        return callback(err);
      }
      callback(null, mappings);
    })
  },

  getFinal(id, callback) {
    if (!callback) {
      throw new Error ('Callback is missing.');
    }
    if (!id) {
      throw new Error ('id is missing.');
    }
    this.mappings.find({id:id, type: "F"}).toArray((err,mappings) => {
      if (err) {
        return callback(err);
      }
      callback(null, mappings);
    })
  },

createIteration(body, callback) {
  if (!body) {
    throw new Error ('body is missing.');
  }
  if (!id) {
    throw new Error ('id is missing.');
  }
  if (!callback) {
    throw new Error ('Callback is missing.');
  }
  const mapping = {
    body: body,
    type: "I"
  };

  this.mappings.insertOne(mapping, err => {
    if (err) {
      return callback(err);
    }
    callback(null);
  });
},
createRoute(body, id, callback) {
  if (!body) {
    throw new Error ('body is missing.');
  }
  if (!id) {
    throw new Error ('id is missing.');
  }
  if (!callback) {
    throw new Error ('Callback is missing.');
  }
  const mapping = {
    body: body,
    id: id,
    type: "J"
  };
  this.mappings.insertOne(mapping, err => {
    if (err) {
      return callback(err);
    }
    callback(null);
  });
},
createSetting(body, id, callback) {
  if (!body) {
    throw new Error ('body is missing.');
  }
  if (!id) {
    throw new Error ('id is missing.');
  }
  if (!callback) {
    throw new Error ('Callback is missing.');
  }

  const mapping = {
    body: body,
    id: id,
    type: "S"
  };
  this.mappings.insertOne(mapping, err => {
    if (err) {
      return callback(err);
    }
    callback(null);
  });
},
getIteration(id, callback) {
  if (!callback) {
    throw new Error ('Callback is missing.');
  }
  if (!id) {
    throw new Error ('id is missing.');
  }
  this.mappings.find({id : id}).toArray((err,mappings) => {
    if (err) {
      return callback(err);
    }
    callback(null, mappings);
  })
},
getIterations(callback) {
  if (!callback) {
    throw new Error ('Callback is missing.');
  }
  this.mappings.find({type:"I"}).toArray((err,mappings) => {
    if (err) {
      return callback(err);
    }
    callback(null, mappings);
  })
},
getRoutes(callback) {
  if (!callback) {
    throw new Error ('Callback is missing.');
  }
  this.mappings.find({type:"J"}).toArray((err,mappings) => {
    if (err) {
      return callback(err);
    }
    callback(null, mappings);
  })
},
getRoute(id, callback) {
  if (!callback) {
    throw new Error ('Callback is missing.');
  }
  if (!id) {
    throw new Error ('id is missing.');
  }
  this.mappings.find({id:id, type: "J"}).toArray((err,mappings) => {
    if (err) {
      return callback(err);
    }
    callback(null, mappings);
  })
},
getJob(id, callback) {
  if (!callback) {
    throw new Error ('Callback is missing.');
  }
  if (!id) {
    throw new Error ('id is missing.');
  }
  this.mappings.find({id:id}).toArray((err,mappings) => {
    if (err) {
      return callback(err);
    }
    callback(null, mappings);
  })
},
getSettings(callback) {
  if (!callback) {
    throw new Error ('Callback is missing.');
  }
  this.mappings.find({type:"S"}).toArray((err,mappings) => {
    if (err) {
      return callback(err);
    }
    callback(null, mappings);
  })
},
getSetting(id, callback) {
  if (!callback) {
    throw new Error ('Callback is missing.');
  }
  if (!id) {
    throw new Error ('id is missing.');
  }
  console.log(id);
  this.mappings.find({id:id, type: "S"}).toArray((err,mappings) => {
    if (err) {
      return callback(err);
    }
    callback(null, mappings);
  })
},
deleteJobs(id, callback) {
  if (!callback) {
    throw new Error ('Callback is missing.');
  }
  if (!id) {
    throw new Error ('id is missing.');
  }
  console.log(id);
  this.mappings.deleteMany({id:id}, (err) => {
    if (err) {
      return callback(err);
    }
    callback(null);
  });
}
};

module.exports = database;
