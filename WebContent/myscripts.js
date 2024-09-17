/**
 * 
 */

function openScreen(screenName) {
	var i, tablinks;
	let screens = document.getElementsByClassName("screenCssClass");
	console.log("screens by class: ", screens)

	for (i = 0; i < screens.length; i++) {
		screens[i].style.display = "none";
	}
	document.getElementById(screenName).style.display = "block";

}
var testData = { "email": "hey@mail.com", "hello": "please run the pyton script to dispence" };
function setScreenSaver(screensaver) {
	if (screensaver) {
		console.log("screensaver ssaver =", document.getElementById("ssaver").style.display);
		console.log("screensaver wrapper =", document.getElementById("wrapper").style.display);
		document.getElementById("ssaver").style.display = "block";
		document.getElementById("wrapper").style.display = "none";

	}
	else {
		console.log("no screensaver ssaver =", document.getElementById("ssaver").style.display);
		console.log("no screensaver wrapper =", document.getElementById("wrapper").style.display);
		document.getElementById("ssaver").style.display = "none";
		document.getElementById("wrapper").style.display = "block";
	}
}
function myCallbackOnClick(data) {
	// alert("Dispenced: "+JSON.stringify(data));
	console.log("and the data is", typeof data, data);
	var callback = document.createElement('p');
	callback.appendChild(
		document.createTextNode('Data: ' + data)
	);
	document.body.append(callback);
}
function onDispenseCallback(originaldata, responsedata) {
	console.log("Reponse: ", originaldata, " ", responsedata);
}
function dispenseButtonClick(buttonnumber) {
	var data = { "number": buttonnumber, "type": "dispense" };
	let p = jsonCall(data, onDispenseCallback);


}

async function jsonCall(data, callback) {
	//alert('You clicked me!');
	try {

		const reqProps = {
			method: 'post',
			headers: {
				'Accept': '*/*'
			},
			body: JSON.stringify(data)
		};
		console.log("about to send to the server", data, callback, reqProps);
		var p1 = fetch(new Request('MyAPIServlet'), reqProps);
		console.log("fetch promise: ", p1);
		var p2 = await p1;
		console.log("second: ", p2);
		var p3 = p2.json();
		console.log("text is ", p3);
		var actext = await p3;
		console.log("actual text is ", actext);
		callback(data, actext);
	}
	catch (error) {
		console.log("error is ", error);
	}

	/*var r=await fetch(new Request('MyAPIServlet'),reqProps
  ).then(function(response) {
		console.log("1. response.json", response);
		if (!response.ok) {throw new Error("HTTP error, status = " + response.status);}
		let b=response.text();

		  return b;
		  }
	).then(function (data) {
		 console.log("2. data.json",typeof data, data);
		callback(data);   
		}
	).catch(function(error) {
		  var p = document.createElement('p');
		  p.appendChild(
			document.createTextNode('Error: ' + error.message)
		  );
		  document.body.insertBefore(p, document.getElementById("button"));
	});*/
	console.log("3. Received: ")
}

const btn = document.getElementById("button");
if (btn != null) btn.addEventListener('click', () => {
	let pElem = document.createElement('p');
	pElem.textContent = 'This is a newly-added paragraph.';
	document.body.appendChild(pElem);
});
