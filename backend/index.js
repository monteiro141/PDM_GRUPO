/* eslint-disable prefer-const */
/* eslint-disable no-trailing-spaces */
/* eslint-disable no-var */
/* eslint-disable require-jsdoc */
/* eslint-disable no-unused-vars */
/* eslint-disable max-len */
/* eslint-disable spaced-comment */

const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp(functions.config().firebase);
const database = admin.database();

exports.completeMatch = functions.region("europe-west1")
    .database.ref("OnComplete/{userId}")
    .onCreate((snapshot, context) =>{
      var user = snapshot.toJSON();
      var userId = user.userid;
      database.ref("Match").once("value").then(function(matchResult) {
        matchResult.forEach( function(innerMatchResult) {
          let currentMatch = innerMatchResult.toJSON();

          /*console.log("Key: "+String(innerMatchResult.key));
          console.log("PartnerOne: "+String(currentMatch.partnerOne));
          console.log("PartnerTwo: "+String(currentMatch.partnerTwo));*/
          if (currentMatch.partnerOne === userId) {
            let partnerId = currentMatch.partnerTwo;
            /**Delete from swipes */
            removeFromSwipes(userId);
            removeFromSwipes(partnerId);
            /**Update matchPending to false on users/id */
            updateMatchPending(userId);
            updateMatchPending(partnerId);
            /**Remove from match */
            removeFromMatch(innerMatchResult.key);
            removeFromOnComplete(userId);
            return;
          } else if (currentMatch.partnerTwo === userId) {
            let partnerId = currentMatch.partnerOne;
            /**Delete from swipes */
            removeFromSwipes(userId);
            removeFromSwipes(partnerId);
            /**Update matchPending to false on users/id */
            updateMatchPending(userId);
            updateMatchPending(partnerId);
            /**Remove from match */
            removeFromMatch(innerMatchResult.key);
            removeFromOnComplete(userId);
            return;
          }
        });
      });
    });

exports.cancelMatch = functions.region("europe-west1")
    .database.ref("OnCancel/{userId}")
    .onCreate((snapshot, context) =>{
      var user = snapshot.toJSON();
      var userId = user.userid;
      //console.log("Key: ->"+String(context.params.key));
      //console.log("json-> "+String(user));
      if (user.state == "Single") {
        //console.log("Single cancel");
        /**Delete from swipes */
        removeFromSwipes(userId);
        /**Update matchPending to false on users/id */
        updateMatchPending(userId);
        /**Remove from queuesingle */
        removeFromQueueSingle(userId);
        /** Remove from onCancel queue*/
        removeFromOnCancel(userId);
        return;
      } else {
        //console.log("Partner cancel");
        /**Delete from swipes */
        removeFromSwipes(userId);
        /**Update matchPending to false on users/id */
        updateMatchPending(userId);
        /** Remove from onCancel queue*/
        removeFromOnCancel(userId);
        return;
      }
    });

exports.matchPendingTest = functions.region("europe-west1")
    .database.ref("Users/{userId}/matchPending")
    .onWrite((change, context) =>{
      const valueMatchPending = change.after.val();
      const userId = context.params.userId;
      if (valueMatchPending) {
        /*Fazer o state = single*/
        let statePromise = getState("Users/"+userId+"/state");
        statePromise.then(function(resultStatePromise) {
          if (resultStatePromise === "Single") {
            //console.log("Teste single");
            matchSingle(userId);
          } else {
            //console.log("Teste couple");
            //console.log(String(resultStatePromise));
            matchPartner(userId, resultStatePromise);
          }
        });
      }
    });

function matchPartner(userid, state) {
  //console.log("Partner email->"+String(state));
  database.ref("Users").once("value").then(function(snapshot) {
    snapshot.forEach(function(innerSnapshot) {
      /*innerSnapshot.key <- userId*/
      const partnerId = innerSnapshot.key;
      let partnerEmailPromise = getState("Users/"+partnerId+"/email");
      partnerEmailPromise.then( function(resultPartnerEmailPromise) {
        if (resultPartnerEmailPromise === state) {
          //console.log("CONSEGUI! -> state e partneremail--->"+String(state)+"|"+String(resultPartnerEmailPromise));
          let partnerMatchPendingPromise = getState("Users/"+partnerId+"/matchPending");
          partnerMatchPendingPromise.then( function(resultPartnerMatchPendingPromise) {
            if (resultPartnerMatchPendingPromise) {
              database.ref("Swipes/"+userid+"/restaurantAccepted").once("value").then( function(resultUserRest) {
                database.ref("Swipes/"+partnerId+"/restaurantAccepted").once("value").then( function(resultPartnerRest) {
                  var restArray = [];
                  /**userJson[firstResult].name */
                  var userJson = resultUserRest.toJSON();
                  var partnerJson = resultPartnerRest.toJSON();
                  Object.keys(userJson).forEach( function( firstResult) {
                    //console.log("Entrou no 1º"+(String(userJson) + "||"+String(firstResult)+"||" + String(userJson[firstResult].name)));
                    Object.keys(partnerJson).forEach( function(secondResult) {
                      //console.log("Entrou no 2º");
                      if (userJson[firstResult].name === partnerJson[secondResult].name &&
                          userJson[firstResult].lat === partnerJson[secondResult].lat &&
                          userJson[firstResult].lng === partnerJson[secondResult].lng) {
                        //console.log("Entrou no 3º");
                        restArray.push(String(userJson[firstResult].name));
                      }
                    });
                  });
                  /**Check if array is empty if yes, remove swipes */
                  var restRandom = restArray[Math.floor(Math.random()*restArray.length)];
                  Object.keys(userJson).forEach( function(randomPicker) {
                    if (restRandom === userJson[randomPicker].name) {
                      //console.log("Entrou com nome: "+String(restRandom));
                      addMatch({partnerOne: userid, partnerTwo: partnerId, name: userJson[randomPicker].name, lat: userJson[randomPicker].lat, lng: userJson[randomPicker].lng, address: userJson[randomPicker].address});
                      return;
                    }
                  });
                });
              });
            }
          });
        }
      });
    });
  });
}

function matchSingle(userid) {
  /** QueueSingle and match it */
  database.ref("Users/"+userid).once("value").then( function(resultUser) {
    var userJson = resultUser.toJSON();
    database.ref("Swipes/"+userid+"/restaurantAccepted").once("value").then( function(resultUserRest) {
      var userRestJson = resultUserRest.toJSON();
      /**Fetch single Uid */
      database.ref("QueueSingle").once("value").then( function(resultQueueSingle) {
        var queueSingle = resultQueueSingle.toJSON();
        var match = false;
        database.ref("Swipes").once("value").then( function(resultAllSwipes) {
          var allSwipes = resultAllSwipes.toJSON();
          Object.keys(queueSingle).forEach( function( keyQueueSingle) {
            if (userJson.gender === queueSingle[keyQueueSingle].interestedIn && userJson.interestedIn === queueSingle[keyQueueSingle].gender && !match) {
              var restArray = [];
              
              Object.keys(userRestJson).forEach( function(firstResult) {
                Object.keys(allSwipes[keyQueueSingle]["restaurantAccepted"]).forEach( function(secondResult) {
                  //console.log("A:"+String(userRestJson[firstResult].name)+"|| B->"+String(allSwipes[keyQueueSingle]["restaurantAccepted"][secondResult].name));
                  if (userRestJson[firstResult].name === allSwipes[keyQueueSingle]["restaurantAccepted"][secondResult].name &&
                      userRestJson[firstResult].lat === allSwipes[keyQueueSingle]["restaurantAccepted"][secondResult].lat &&
                      userRestJson[firstResult].lng === allSwipes[keyQueueSingle]["restaurantAccepted"][secondResult].lng) {
                    //console.log("Entrou no 3º");
                    restArray.push(String(userRestJson[firstResult].name));
                  }
                });
              });
              if (restArray.length != 0) {
                match = true;
                var restRandom = restArray[Math.floor(Math.random()*restArray.length)];
                Object.keys(userRestJson).forEach( function(randomPicker) {
                  if (restRandom === userRestJson[randomPicker].name) {
                  //console.log("Entrou com nome: "+String(restRandom));
                    addMatch({partnerOne: userid, partnerTwo: keyQueueSingle, name: userRestJson[randomPicker].name, lat: userRestJson[randomPicker].lat, lng: userRestJson[randomPicker].lng, address: userRestJson[randomPicker].address});
                    addQueueSingle(null, keyQueueSingle);
                    return;
                  }
                });
              }
            }
          });
          /**Add to queue single if no match is found */
          if (!match) {
            addQueueSingle({gender: userJson.gender, interestedIn: userJson.interestedIn}, userid);
            return;
          }
        });
      });
    });
  });
}

function addQueueSingle(value, uid) {
  const rootRef = database.ref();
  const storesRef = rootRef.child("QueueSingle/"+uid);
  storesRef.set(value);
}

function addMatch(value) {
  const rootRef = database.ref();
  const storesRef = rootRef.child("Match");
  const newStoreRef = storesRef.push();
  newStoreRef.set(value);
}

function getState(path) {
  return database.ref(path)
      .once("value")
      .then(function(snapshot) {
        return snapshot.val();
      });
}

function removeFromSwipes(uid) {
  const rootRef = database.ref();
  const storesRef = rootRef.child("Swipes/"+uid);
  storesRef.set(null);
}
function updateMatchPending(uid) {
  const rootRef = database.ref();
  const storesRef = rootRef.child("Users/"+uid);
  storesRef.update({matchPending: false});
}

function removeFromQueueSingle(uid) {
  const rootRef = database.ref();
  const storesRef = rootRef.child("QueueSingle/"+uid);
  storesRef.set(null);
}

function removeFromOnCancel(uid) {
  const rootRef = database.ref();
  const storesRef = rootRef.child("OnCancel/"+uid);
  storesRef.set(null);
}

function removeFromOnComplete(uid) {
  const rootRef = database.ref();
  const storesRef = rootRef.child("OnComplete/"+uid);
  storesRef.set(null);
}

function removeFromMatch(uid) {
  const rootRef = database.ref();
  const storesRef = rootRef.child("Match/"+uid);
  storesRef.set(null);
}
