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
