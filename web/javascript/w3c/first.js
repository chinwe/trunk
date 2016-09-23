function displayDate(){
	document.getElementById("demo").innerHTML = Date();
}

function changeColor(){
	stylep = document.getElementById("style");
	stylep.style.color = "#FF0000";
}

function checkValid() {
	var x = 12;

	if (!isNaN(x)) {
		alert("NaN");
	}	
}