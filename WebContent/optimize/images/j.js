function addCommas (nStr) {
    nStr += '';
    var x = nStr.split('.');
    var x1 = x[0];
    var x2 = x.length > 1 ? '.' + x[1] : '';
    var rgx = /(\d+)(\d{3})/;
    while (rgx.test(x1)) {
        x1 = x1.replace(rgx, '$1' + ',' + '$2');
    }
    return x1 + x2;
}

function handleError(key, statusText) {
	var rows = document.getElementsByClassName(key),
	    errorMsg = (statusText ? statusText : "Error optimizing images") + '. Please email <a href="mailto:eperret@salesforce.com">Eric Perret</a> with the file you are trying to optimize.';
	for(var i = 0, row; row = rows[i++]; ) {
		row.deleteCell(4);
		row.deleteCell(4);
		row.cells[3].colSpan = 3;
		row.cells[3].innerHTML = errorMsg;
	}
}

function FileUpload(fileData, key) {
	var reader = new FileReader();  
	//this.ctrl = createThrobber(img);
	var xhr = new XMLHttpRequest();
	this.xhr = xhr;

//	var self = this;
//	this.xhr.upload.addEventListener("progress", function(e) {
//		if (e.lengthComputable) {
//			var percentage = Math.round((e.loaded * 100) / e.total);
//			//self.ctrl.update(percentage);
//		}
//	}, false);
//	xhr.upload.addEventListener("load", function(e){
//		//
//	}, false);
	xhr.open("POST", "do/upload");
	xhr.overrideMimeType('text/plain; charset=x-user-defined-binary');
	reader.onload = function(evt) {
		xhr.sendAsBinary(evt.target.result);
	};
	xhr.onload = function(e) {
		if(xhr.status == 200) {
			parseResults(JSON.parse(xhr.response), key);
		} else {
			handleError(key, xhr.statusText);
		}
	};
	xhr.onerror = function(e) {
		//Add better error messages from the server
		handleError(key, xhr.statusText);
	};
	//reader.readAsBinaryString(file);
	
	var form = document.forms[0];
	fileData.append(form.webp.name, form.webp.checked);
	fileData.append(form.conversion.name, form.conversion.value);
	
	xhr.send(fileData);
}

var supportedFileTypes = ["image/gif", "image/png", "image/jpeg"];

function isAllowed(file) {
	if(supportedFileTypes.indexOf(file.type) == -1) {
		return "Unsupported File Type";
	} else if(file.size > (2 * 1024 * 1024)) {
		return "Image cannot be larger than 2 MB";
	}
	return "";
}

function parseFile(file, id) {
	var filesDiv = document.getElementById("files"),
		validatedFile = null;
	if(!filesDiv.innerHTML.length) {
		filesDiv.innerHTML = '<table><thead><th sope="col">Name</th><th sope="col" style="width:5em">Type</th><th sope="col" style="width:4em">Starting Size</th><th sope="col" style="width:4em">Ending Size</th><th sope="col" style="width:4em">Savings</th><th sope="col">Download</th></thead><tbody id="bdy"></tbody></table>';
	}

	var row = document.createElement("tr"),
        message = isAllowed(file);
	
	if(message) {
		row.className="unsupported";
		row.innerHTML = "<td>" + file.name + "</td><td>" + file.type + "</td><td>" + addCommas(file.size) + '</td><td colspan="3">' + message + "</td>";
	} else {
		validatedFile = file;
		row.innerHTML = "<td>" + file.name + "</td><td>" + file.type + "</td><td>" + addCommas(file.size) + '</td><td></td><td></td><td><img alt="loading" src="../barloading.gif"/></td>';
		row.className = id;
		row.setAttribute("data-file", file.name);
	}
	
	document.getElementById("bdy").appendChild(row);
	return validatedFile;
}

function fileDragHover(e) {
	e.stopPropagation();
	e.preventDefault();
	e.target.className = (e.type == "dragover" ? "hover" : "");
}

function parseResults (data, key) {
	var rows = document.getElementsByClassName(key);
	for(var i = 0; i < rows.length; i++) {
		var row = rows[i];
		var result = data[row.getAttribute("data-file")],
			resultI;
		if(result && result.length) {
			resultI = result[0];
			row.cells[3].innerHTML = addCommas(resultI.size);
			row.cells[4].innerHTML = addCommas(resultI.savings);
			row.cells[5].innerHTML = '<a href="do/get/' + resultI.id + "/" + resultI.file + '">' + resultI.file + "</a>";
			if(resultI = result[1]) {
				row.cells[0].rowSpan = row.cells[1].rowSpan = row.cells[2].rowSpan = 2;
				row = row.parentNode.insertRow(row.rowIndex);
				row.innerHTML = "<td>" + addCommas(resultI.size) + "</td><td>" + addCommas(resultI.savings) + '</td><td><a href="do/get/' + resultI.id + "/" + resultI.file + '">' + resultI.file + "</a></td>";
			}
		} else {
			row.deleteCell(4);
			row.deleteCell(4);
			row.cells[3].colSpan = 3;
			row.cells[3].innerHTML = '<span style="font-weight:800">Already Optimized</span> (or server had an error)';
		}
	}
}

//file selection
(function() {
	var key = 1;
	window.FileSelectHandler = function(e) {
		var data = new FormData();
		
		// cancel event and hover styling
		fileDragHover(e);
		// fetch FileList object
		var files = (e.target.files || e.dataTransfer.files),
			i = 0,
			f;
		// process all File objects
		for (; f = files[i]; i++) {
			f = parseFile(f, "a" + key);
			if(f) {
				data.append("file[" + i + "]", f);
			}
		}
		new FileUpload(data, "a" + key);
		key++;
	};
})();

// initialize
function Init() {
	var fileselect = document.getElementById("fileselect"),
		filedrag = document.getElementById("filedrag"),
		submitbutton = document.getElementById("submitbutton");

	// file select
	fileselect.addEventListener("change", FileSelectHandler, false);

	// is XHR2 available?
	var xhr = new XMLHttpRequest();
	if (xhr.upload) {
	
		// file drop
		filedrag.addEventListener("dragover", fileDragHover, false);
		filedrag.addEventListener("dragleave", fileDragHover, false);
		filedrag.addEventListener("drop", FileSelectHandler, false);
		filedrag.style.display = "block";
		
		// remove submit button
		submitbutton.style.display = "none";
	}
}

// call initialization file
if (window.File && window.FileList && window.FileReader) {
	Init();
}