var Map = React.createClass( {
    getInitialState: function() {
       return {sensorMarkers: []}
    },
  componentDidMount: function() {
    var nashville = {lat: 36.1627, lng: -86.7816};
    this.map = new google.maps.Map(this.refs.map, {
      zoom: 10,
      center: nashville
    });
  },
  componentWillReceiveProps: function(newProps){
    this.removeAllMarkers();
    if(newProps.sensors!=undefined){
        for(var i=0;i<newProps.sensors.length;i++){
            var sensor = {lat: parseFloat(newProps.sensors[i].lat), lng: parseFloat(newProps.sensors[i].long)};
            var marker;
            if(newProps.sensors[i].state=="clear"){
                marker = new google.maps.Marker({
                  position: sensor,
                  map: this.map,
                  icon: "https://maps.google.com/mapfiles/ms/micons/water.png"
                  //label: "S"+newProps.sensors[i].sensor,
                });
            } else if(newProps.sensors[i].state="pending_task"){
                marker = new google.maps.Marker({
                  position: sensor,
                  map: this.map,
                  icon: "https://maps.google.com/mapfiles/ms/micons/hotsprings.png"
                  //label: "S"+newProps.sensors[i].sensor,
                });
            } else {
                marker = new google.maps.Marker({
                  position: sensor,
                  map: this.map,
                  icon: "https://maps.google.com/mapfiles/ms/micons/mechanic.png"
                  //label: "S"+newProps.sensors[i].sensor,
                });
            }
            var infowindow = new google.maps.InfoWindow({ content: "<p>"+newProps.sensors[i].sensor+"</p>" });
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
                  icon: "https://maps.google.com/mapfiles/ms/micons/truck.png"
                });
                infowindow = new google.maps.InfoWindow({ content: "<p>"+newProps.users[i].firstName+" currently has no tasks</p></br><p>Their tank is full</p>" });
            } else {
                marker = new google.maps.Marker({
                  position: sensor,
                  map: this.map,
                  icon: "http://maps.google.com/mapfiles/kml/pal4/icon15.png"
                });
                var myDate = new Date(newProps.users[i].activeTask.start_date);
                infowindow = new google.maps.InfoWindow({ content: "<p>"+newProps.users[i].firstName+" started a new task at "+myDate+"</p></br><p>They are headed to Pad"+newProps.users[i].activeTask.sensor+"</p>" });
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
        <div ref="map" style={mapStyle} >I should be a map!</div>
      </div>
    );
  }
});

export default Map