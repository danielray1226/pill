async function pullServerEvents(onDataAvailable) {
	//alert('You clicked me!');
	console.log("calling ")
	const reqProps = {
		method: 'post',
		headers: {
			'Accept': '*/*'
		},
		mode: 'cors',
		cache: 'default',
		body: "{}"
	};

	let p = fetch(new Request('PushServlet'), reqProps).then(
		function(response) {
			if (!response.ok) { throw new Error("HTTP error, status = " + response.status); }
			let b = response.json();
			console.log("response.json", typeof b, b);
			return b;
		}
	);
	console.log("promise: ", p);
	let data = await p;
	console.log("data: ", data);
	onDataAvailable(data);
	setTimeout(pullServerEvents, 0, onDataAvailable);
	console.log("called ")
}
