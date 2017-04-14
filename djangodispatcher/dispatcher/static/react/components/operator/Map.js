var Map = React.createClass( {
    getInitialState: function() {
       return {sensorMarkers: []}
    },
  componentDidMount: function() {
    var nashville = {lat: 36.1627, lng: -86.7816};
    this.map = new google.maps.Map(this.refs.map, {
      zoom: 11,
      center: nashville
    });
  },
  componentWillReceiveProps: function(newProps){
    if(newProps.user != this.props.user){
        var center = {lat: parseFloat(newProps.user.lat), lng: parseFloat(newProps.user.long)};
        this.map.panTo(center);
    }
    this.removeAllMarkers();
    var green = new google.maps.MarkerImage("/static/react/greeniconbubble.png", null, null, null, new google.maps.Size(30,50));
    var yellow = new google.maps.MarkerImage("/static/react/yellowiconbubble.png", null, null, null, new google.maps.Size(30,50));
    var red = new google.maps.MarkerImage("/static/react/rediconbubble.png", null, null, null, new google.maps.Size(30,50));
    var redaccepted = new google.maps.MarkerImage("/static/react/rediconbubbleoutline.png", null, null, null, new google.maps.Size(30,50));
    var truck = new google.maps.MarkerImage("/static/react/truckblue.png", null, null, null, new google.maps.Size(60,30));
    var truckgreen = new google.maps.MarkerImage("/static/react/truckgreen.png", null, null, null, new google.maps.Size(60,30));
    if(newProps.sensors!=undefined){
        for(var i=0;i<newProps.sensors.length;i++){
            var sensor = {lat: parseFloat(newProps.sensors[i].lat), lng: parseFloat(newProps.sensors[i].long)};
            var marker = new google.maps.Marker({
                  position: sensor,
                  map: this.map,
                  animation: google.maps.Animation.DROP,
                });
            var info = "<p>Pad "+newProps.sensors[i].sensor+"</p>";
            info += "<p>"+newProps.sensors[i].waterLevel+" of "+newProps.sensors[i].waterCapacity+"</p>"
            var ratio = newProps.sensors[i].waterLevel/newProps.sensors[i].waterCapacity;
            if(ratio < .45){
                //marker.setIcon("http://maps.google.com/mapfiles/ms/icons/green-dot.png",);
                marker.setIcon(green);
                info += "<p>There are no workers needed</p>";
            } else if(ratio < .70){
                //marker.setIcon('http://maps.google.com/mapfiles/ms/icons/yellow-dot.png');
                marker.setIcon(yellow);
                info += "<p>There are no workers needed</p>";
            } else {
                if(newProps.sensors[i].state=="being_fixed"){
                    marker.setIcon(redaccepted);
                    info += "<p>A worker is on their way</p>";
                } else {
                    marker.setIcon(red);
                    info += "<p>Waiting for a worker to accept the haul</p>";
                }
            }
            //for(var j = 0; j<newProps.sensors[i].wells.length;j++){
            //    var well = newProps.sensors[i].wells[j];
            //    info += "</br><p>Well "+(j+1)+": "+100*well.level/well.capacity+"% Full</p>"
           // }
            var infowindow = new google.maps.InfoWindow({ content: info });
            this.bindInfoWindow(marker, this.map, infowindow)
            this.state.sensorMarkers.push(marker)
        }
    }
    if(newProps.users!=undefined){
        for(var i=0;i<newProps.users.length;i++){
            var sensor = {lat: parseFloat(newProps.users[i].lat), lng: parseFloat(newProps.users[i].long)};
            var marker;
            var infowindow;
            if(newProps.users[i].activeTask == null){
                marker = new google.maps.Marker({
                  position: sensor,
                  map: this.map,
                  icon: truck, //image//"https://maps.google.com/mapfiles/ms/micons/truck.png"
                });
                infowindow = new google.maps.InfoWindow({ content: "<p>"+newProps.users[i].firstName+
                " currently has no tasks</p></br><p>Their tank has room for "+ newProps.users[i].truckAvailableCapacity+" barrels</p>" });
            } else {
                marker = new google.maps.Marker({
                  position: sensor,
                  map: this.map,
                  icon: truckgreen, //"http://maps.google.com/mapfiles/kml/pal4/icon15.png"
                });
                var myDate = new Date(newProps.users[i].activeTask.start_date);
                infowindow = new google.maps.InfoWindow({ content: "<p>"+newProps.users[i].firstName+" started a new task at "+myDate+"</p></br><p>They are headed to Pad "+newProps.users[i].activeTask.sensor+"</p>" });
            }
            this.bindInfoWindow(marker, this.map, infowindow)
            this.state.sensorMarkers.push(marker)
        }
    }
  },
  removeAllMarkers: function(){
    for (var i = 0; i < this.state.sensorMarkers.length; i++) {
          this.state.sensorMarkers[i].setMap(null);
        }
  },
  bindInfoWindow: function(marker, map, infowindow) {
        marker.addListener('click', function() {
            infowindow.open(map, this);
        });
    },
  render: function() {
    const mapStyle = {
      width: '100%',
      height: 540,
      border: '1px solid black'
    };

    return (
      <div className="text-center">
        <div ref="map" style={mapStyle}>I should be a map!</div>
      </div>
    );
  }
});

export default Map