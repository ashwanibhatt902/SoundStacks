var Image = require("parse-image");
// Use Parse.Cloud.define to define as many cloud functions as you want.
// For example:
Parse.Cloud.define("hello", function(request, response) {
  response.success("Hello world!");
});

Parse.Cloud.define("scaleProfileImage", function(request, response) {

    console.log("image conversion called");
    var user  = request.user;

    if (!user) {
        console.log("no user");
        return response.success();
    }

    var Image = require("parse-image");

    if (!user.get("profilePicFile")) {
       response.error("Users must have a profile photo.");
           return;
    }

//    if (!user.dirty("profilePicFile")) {
//        response.success();
//        return;
//    }

    Parse.Cloud.httpRequest({
        url: user.get("profilePicFile").url()

    }).then(function(response) {
        var image = new Image();
        return image.setData(response.buffer);

    }).then(function(image) {
        // Resize the image to 144x1444.
        return image.scale({
          width: 144,
          height: 144
        });

    }).then(function(image) {
        // Make sure it's a JPEG to save disk space and bandwidth.
        return image.setFormat("JPEG");

    }).then(function(image) {
        // Get the image data in a Buffer.
        return image.data();

    }).then(function(buffer) {
        // Save the image into a new file.
        var base64 = buffer.toString("base64");
        var cropped = new Parse.File("thumbnail.jpg", { base64: base64 });
        console.log("after scale"+cropped.url())
        return cropped.save();

    }).then(function(cropped) {
        // Attach the image file to the original object.
        console.log("after scale"+cropped.url())
        user.set("profilePicFileLQ", cropped);
        user.save();

    }).then(function(result) {
        console.log("image conversion started");
        return response.success();

      }, function(error) {
        console.log("image conversion failed");
         return response.error(error);
    });

    console.log("image conversion started");

});

Parse.Cloud.define("uploadImageBackend", function(request, response) {
  Parse.Cloud.httpRequest({
      method: 'POST',
      url: "https://api.parse.com/1/jobs/uploadImageJob",
      body: {
          'userID' : request.params.userID,
          'profilePicURL' : request.params.profilePicURL,
          'coverPicURL' : request.params.coverPicURL,
        },
        headers: {
            'Content-Type': 'application/json;charset=utf-8',
            'X-Parse-Application-Id' : 'UnUpW6Hpn6z5iB1riCaPj54sbMlGToLpkyQoLyES',
            'X-Parse-Master-Key' : '1GW9Wb0Vaz48sK8WtxlY1VkNt9oomZFhVHAQfWLm'
          },
      success: function(response1) {
           response.success('started backgroud job'+response1);
      },
      error: function(error) {
        // The networking request failed.
        console.log(error);
        console.log(error.code);
        console.log(error.error);
         response.error("Uh oh, something went wrong."+error);
      }
    });

    response.success('started backgroud job'+response1);

});

var _ = require("underscore");

var toLowerCase = function(w) { return w.toLowerCase(); };

Parse.Cloud.beforeSave("Sounds", function(request, response) {

    var sound  = request.object;
    if (sound.existed()) {
        console.log("old sound");
        return response.success();
    }



    var allTags = Parse.Object.extend("Tags");
    var query = new Parse.Query(allTags);

    query.get("2KXtxw54dl", {
      success: function(tag) {

      console.log("objectId"+tag);
         var tags = request.object.get("tags");
         var tagsAsString = "";
         if (!tags) {
            return response.success();
         }

         for (var i = 0; i< tags.length; i++) {
            var lowerTag = tags[i].toLowerCase();
            tag.addUnique("soundTags", lowerTag);
            tagsAsString = tagsAsString + lowerTag+  " ";
         }

        tag.save();

         var name =  sound.get("name");
        name = name.toLowerCase();

        sound.addUnique("tags", name);

        tagsAsString = tagsAsString + " " + name;
        sound.set("tagsAsString", tagsAsString);

        return response.success();
      },
      error: function(object, error) {
        console.log("error");
        return response.success();

      }
    });
});


Parse.Cloud.beforeSave("Categories", function(request, response) {

    var category  = request.object;
    if (category.existed()) {
        console.log("old category");
        return response.success();
    }

    var GameScore = Parse.Object.extend("Tags");

    var query = new Parse.Query(GameScore);
    query.get("2KXtxw54dl", {
      success: function(gameScore) {
         console.log("objectId"+gameScore);
         var tags = request.object.get("tags");
         var tagsAsString = "";

         if (!tags) {
            return response.success();
         }

         for (var i = 0; i< tags.length; i++) {
            var lowerTag = tags[i].toLowerCase();
            gameScore.addUnique("categoryTags", lowerTag);
            tagsAsString = tagsAsString + lowerTag +  " ";
         }
        gameScore.save();

        var name =  category.get("name");
        name = name.toLowerCase();

        category.addUnique("tags", name);

        tagsAsString = tagsAsString + " " + name;
        category.set("tagsAsString", tagsAsString);

        return response.success();

      },
      error: function(object, error) {
        console.log("error");
        return response.success();

      }
    });
});


Parse.Cloud.beforeSave(Parse.User, function(request, response) {

    var user  = request.object;
    if (user.existed()) {
		console.log("old user");
		if(user.dirtyKeys() && user.dirty("profilePicFile")){
			console.log("scalinf down image");
			Parse.Cloud.httpRequest({
                                               url: user.get("profilePicFile").url()
                                               
                                               }).then(function(response) {
                                                       var image = new Image();
                                                       return image.setData(response.buffer);
                                                       
                                                       }).then(function(image) {
															   var w = 200; 
															   var h = 200 * (image.height()/image.width()); 
                                                               return image.scale({
                                                                                  width: w,
																				  height: h			
                                                                                  });
                                                               
                                                               }).then(function(image) {
                                                                       // Make sure it's a JPEG to save disk space and bandwidth.
                                                                       return image.setFormat("JPEG");
                                                                       
                                                                       }).then(function(image) {
                                                                               // Get the image data in a Buffer.
                                                                               return image.data();
                                                                               
                                                                               }).then(function(buffer) {
                                                                                       // Save the image into a new file.
                                                                                       var base64 = buffer.toString("base64");
                                                                                       var cropped = new Parse.File("thumbnail.jpg", { base64: base64 });
                                                                                       return cropped.save();
                                                                                       
                                                                                       }).then(function(cropped) {
                                                                                               // Attach the image file to the original object.
                                                                                               user.set("profilePicFileLQ", cropped);
                                                                                               
                                                                                               }).then(function(result) {
																									  console.log("success");
                                                                                                       return response.success();
                                                                                                       }, function(error) {
																																																																							console.log("error");
                                                                                                       return response.error(error);
                                                                                                       });
		
		}else{
			return response.success();
		}
		
		
    }else{
	console.log("hererreakjdhkjas askdjhakjsdhkja ");
    var query = new Parse.Query(Parse.User);
    query.equalTo("name", request.object.get("name"));
    query.count({
      success: function(count) {
        if (count == 0) {
            console.log("count 1"+count);
            response.success();
        }
        else {
            var num = Math.floor(Math.random() * 900) + 100;
            var query1 = new Parse.Query(Parse.User);
            query1.equalTo("name", request.object.get("name")+num);

            console.log("second name"+num);
            query1.count({
              success: function(count) {
                // The count request succeeded. Show the count
                if (count == 0) {
                    request.object.set("name", request.object.get("name")+num);
                    response.success();
                }
                else {
                    request.object.set("");
                    response.success();
                }
              },
              error: function(error) {
                // The request failed
                console.log("error");
              }
            });
        }
      },
      error: function(error) {
        // The request failed
        console.log("error");
      }
    });
	}
});


//Parse.Cloud.afterSave(Parse.User, function(request) {
//
//    Parse.Cloud.useMasterKey();
//    var user  = request.object;
//    if (user.existed()) {
//        console.log("old User")
//        return;
//    }
//    if (request.object.get("name") == "") {
//        var userID  = request.object.id;
//        request.object.set("name", userID);
//
//        var query = new Parse.Query(Parse.User);
//        query.get(userID, {
//            success: function(user) {
//                console.log("find user");
//                user.set("name", userID);
//                user.save();
//            }
//        });
//        console.log("empty name");
//        console.log(userID);
//    }
//    console.log("Finish")
//});


Parse.Cloud.job("uploadImageJob", function(request, status) {
      // Set up to modify user data
      Parse.Cloud.useMasterKey();

      console.log(request.params);

      // Query for all users
      var userID = request.params.userID;
      var profilePicURL = request.params.profilePicURL;
      var coverPicURL = request.params.coverPicURL;

        if (!profilePicURL  && !coverPicURL ) {
            status.error("Uh oh, no file to upload");
        }

      var query = new Parse.Query(Parse.User);

      query.get(userID, {
            success: function(user) {
            console.log("user is", user.get('name'));

              Parse.Cloud.httpRequest({
                url: profilePicURL,
                success: function(response1) {
                    console.log(response1);
                    var file = new Parse.File("profilePicFile.jpg", {base64: response1.buffer.toString('base64')});

                    file.save().then(function() {
                      // The file has been saved to Parse.

                      user.set("profilePicFile", file);
                      user.save();

                      Parse.Cloud.httpRequest({
                        url: coverPicURL,
                        success: function(response1) {
                            console.log(response1);
                            var file = new Parse.File("coverPicFile.jpg", {base64: response1.buffer.toString('base64')});

                            file.save().then(function() {
                              // The file has been saved to Parse.

                              user.set("coverPicFile", file);
                              user.save();
                              status.success("Migration completed successfully.")
                            }, function(error) {
                              status.error("Uh oh, something went wrong.");
                            });


                        },
                        error: function(error) {
                          // The networking request failed.
                           status.error("Uh oh, something went wrong.");
                        }
                      });

                    }, function(error) {
                      status.error("Uh oh, something went wrong.");
                    });


                },
                error: function(error) {
                  // The networking request failed.
                   status.error("Uh oh, something went wrong.");
                }
              });



            },
            error: function(error) {
             status.error("Uh oh, something went wrong.");
            }
        });

});

Parse.Cloud.job("fixDBForSearching", function(request, status) {
      // Set up to modify user data
      Parse.Cloud.useMasterKey();

      console.log(request.params);


var GameScore = Parse.Object.extend("Tags");

    var query = new Parse.Query(GameScore);
    query.get("2KXtxw54dl", {
      success: function(gameScore) {


         var soundQuery = new Parse.Query("Sounds");
         var categoryQuery = new Parse.Query("Categories");

         soundQuery.limit(1000);
         categoryQuery.limit(1000);

            soundQuery.find({
              success: function(results) {
                console.log("Successfully retrieved " + results.length + " sound.");
                // Do something with the returned Parse.Object values
                for (var i = 0; i < results.length; i++) {
                  var object = results[i];
                  var soundTags = object.get("tags");
                  var tagsAsString = "";

                  if (soundTags) {
//                    gameScore.addUnique("soundTags", soundTags);


                             for (var j = 0; j< soundTags.length; j++) {
                                gameScore.addUnique("soundTags", soundTags[j]);
                                var lowerTag = soundTags[j].toLowerCase();
                                tagsAsString = tagsAsString + lowerTag +  " ";
                             }
                            gameScore.save();
                  }

                   var name =  object.get("name");
                      name = name.toLowerCase();


                      tagsAsString = tagsAsString + name;
                      object.set("tagsAsString", tagsAsString);
                      object.save();

                }
              },
              error: function(error) {
                alert("Error: " + error.code + " " + error.message);
              }
            });

             categoryQuery.find({
              success: function(results) {
                console.log("Successfully retrieved " + results.length + " category.");
                // Do something with the returned Parse.Object values


                for (var i = 0; i < results.length; i++) {
                  var object = results[i];
                  var tagsAsString = "";

                    var soundTags = object.get("tags");
                    if (soundTags) {
//                      gameScore.addUnique("categoryTags", soundTags);


                               for (var j = 0; j< soundTags.length; j++) {
                                gameScore.addUnique("categoryTags", soundTags[j]);
                                  var lowerTag = soundTags[j].toLowerCase();
                                  tagsAsString = tagsAsString + lowerTag +  " ";
                               }
                              gameScore.save();

                    }
                     var name =  object.get("name");
                      name = name.toLowerCase();


                      tagsAsString = tagsAsString + name;
                      object.set("tagsAsString", tagsAsString);
                      object.save();

                }
              },
              error: function(error) {
                alert("Error: " + error.code + " " + error.message);
              }
            });


        }
        });
});