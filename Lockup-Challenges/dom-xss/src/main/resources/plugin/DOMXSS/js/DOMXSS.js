function displayGreeting(name) {
	if (name != ''){
		name = name.replace("<","");
		name = name.replace(">", "");
		name = name.replace("'", "");
		name = name.replace("\"", "");
		document.getElementById("greeting").innerHTML="Hello, " + name+ "!";

	}
}