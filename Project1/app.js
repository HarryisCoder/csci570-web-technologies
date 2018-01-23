var express = require('express');
var request = require('request');
var cors = require('cors');
var parser = require('xml2json');
// var parseString = require('xml2js').parseString;
var port = process.env.PORT || 3000;
// var port = 9000;

// console.log(123);
var app = express();
app.use(cors());

app.get('/', function (req, res) {
	console.log("Req: " + req + "#");
	var symbol = req.query.symbol.trim();
	var indicator = req.query.indicator.trim();
	console.log("Indicator: " + indicator + "#");
	console.log("Symbol: " + symbol + "#");
	if (typeof symbol !== 'undefined') {
		if (indicator !== 'news') {
			// console.log("Request " + indicator + " ...");
			var url = "https://www.alphavantage.co/query?function=" + indicator + "&symbol=" + symbol + "&outputsize=full&interval=daily&time_period=10&series_type=open&apikey=A4QY6DLFJIPJZOL7";

			request(url, function (error, response, body) {
				console.log('statusCode:', response && response.statusCode);
				if (!error && response.statusCode == 200) {
				    var json = JSON.parse(body);
				    // parseString(body, function (err, result) {
		              	// console.dir(result);
		            //   	console.log("Send JSON successfully!");
		            //   	res.send(result);
		            //   	res.end();
		            // });
				    console.log("Send JSON successfully!");
				    
					// console.log(json);
					res.send(json);
					res.end();
				    // }
				}
			});
		} else {
			// console.log("Request " + symbol + " ...");
			var url = "https://seekingalpha.com/api/sa/combined/" + symbol + ".xml";

			request(url, function (error, response, body) {
				console.log('statusCode:', response && response.statusCode);
				if (!error && response.statusCode == 200) {
					var json = parser.toJson(body);
				    // var json = JSON.parse(body);
				    // parseString(body, function (err, result) {
				    // 	console.dir(result);
				    // 	console.log("Send json successfully!");
					   //  // console.log(json);
					   //  res.send(result);
					   //  res.end();
				    // });
				    console.log("Send json successfully!");
				    // console.log(json);
				    res.send(json);
				    res.end();
				    
				} else {
					res.send("error");
				    res.end();
				}
			});
		}
	} else {
		console.log("[DEBUG]Symbol undefined!");
	}

});

// app.listen(process.env.PORT, () => console.log('Example app listening on port!'))

// Listen on port 3000, IP defaults to 127.0.0.1
app.listen(port, function(){
	// Put a friendly message on the terminal
	console.log('Server running at http://127.0.0.1:' + port + '/');
});
