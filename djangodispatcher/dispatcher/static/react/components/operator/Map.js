var Map = React.createClass( {
    getInitialState: function() {
       return {sensorMarkers: []}
    },
  componentDidMount: function() {
    var nashville = {lat: 36.1627, lng: -86.7816};
    //var nashville = {lat: parseFloat(this.props.user.lat), lng: parseFloat(this.props.user.long)};
    console.log(nashville);
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
    var image = new google.maps.MarkerImage("/static/react/OilAndGas-PossibleLogov3.png", null, null, null, new google.maps.Size(20,30));
    var goldStar = {
        path: "M0-48c-9.8 0-17.7 7.8-17.7 17.4 0 15.5 17.7 30.6 17.7 30.6s17.7-15.4 17.7-30.6c0-9.6-7.9-17.4-17.7-17.4z",//'M 125,5 155,90 245,90 175,145 200,230 125,180 50,230 75,145 5,90 95,90 z',
        fillColor: '#ff0000',
        fillOpacity: 0.8,
        scale: 4,
        strokeColor: '#ff0000',
        strokeWeight: 5
      };

    if(newProps.sensors!=undefined){
        for(var i=0;i<newProps.sensors.length;i++){
            var sensor = {lat: parseFloat(newProps.sensors[i].lat), lng: parseFloat(newProps.sensors[i].long)};
            var marker;
            if(newProps.sensors[i].state=="clear"){
                marker = new google.maps.Marker({
                  position: sensor,
                  map: this.map,
                  animation: google.maps.Animation.DROP,
                  icon: "http://maps.google.com/mapfiles/ms/icons/green-dot.png"
                });
            } else if(newProps.sensors[i].state="pending_task"){
                marker = new google.maps.Marker({
                  position: sensor,
                  map: this.map,
                  animation: google.maps.Animation.DROP,
                  icon: goldStar,
                  //icon: image//"http://maps.google.com/mapfiles/ms/icons/red-dot.png"
                });
            } else {
                marker = new google.maps.Marker({
                  position: sensor,
                  map: this.map,
                  icon: 'http://maps.google.com/mapfiles/ms/icons/yellow-dot.png'
                });
            }
            var info = "<p>Pad "+newProps.sensors[i].sensor+"</p>";
            for(var j = 0; j<newProps.sensors[i].wells.length;j++){
                var well = newProps.sensors[i].wells[j];
                info += "</br><p>Well "+(j+1)+": "+100*well.level/well.capacity+"% Full</p>"
            }
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
                  icon: image//"https://maps.google.com/mapfiles/ms/micons/truck.png"
                });
                infowindow = new google.maps.InfoWindow({ content: "<p>"+newProps.users[i].firstName+" currently has no tasks</p></br><p>Their tank is full</p>" });
            } else {
                marker = new google.maps.Marker({
                  position: sensor,
                  map: this.map,
                  icon: "http://maps.google.com/mapfiles/kml/pal4/icon15.png"
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
        <div ref="map" style={mapStyle} >I should be a map!</div>
      </div>
    );
  }
});

export default Map