<?xml version="1.0" encoding="UTF-8"?>
<tileset version="1.10" tiledversion="1.11.2" name="Interior" tilewidth="32" tileheight="32" tilecount="256" columns="16">
 <image source="Main_Spritesheet.png" width="512" height="512"/>
 <tile id="5">
  <properties>
   <property name="z" type="int" value="4"/>
  </properties>
 </tile>
 <tile id="24">
  <objectgroup draworder="index" id="3">
   <object id="2" x="0" y="16" width="32" height="16"/>
  </objectgroup>
 </tile>
 <tile id="29" type="Prop">
  <properties>
   <property name="direction" value="DOWN"/>
   <property name="restitution" type="float" value="1"/>
   <property name="sensor" type="bool" value="true"/>
   <property name="speed" type="float" value="2"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="2" x="32" y="32">
    <polygon points="0,0 -22.9375,-0.09375 -22.0625,-10.25 -9.75,-16.25 0,-16"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="30" type="Prop">
  <properties>
   <property name="direction" value="LEFT"/>
   <property name="restitution" type="float" value="1"/>
   <property name="sensor" type="bool" value="true"/>
   <property name="speed" type="float" value="2"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="16" width="32" height="16"/>
  </objectgroup>
 </tile>
 <tile id="31" type="Prop">
  <properties>
   <property name="direction" value="LEFT"/>
   <property name="restitution" type="float" value="1"/>
   <property name="sensor" type="bool" value="true"/>
   <property name="speed" type="float" value="2"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="2" x="0" y="32">
    <polygon points="0,0 0,-16.0976 16.9867,-16.0253 22.408,-9.3752 22.5525,-0.195166"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="35">
  <properties>
   <property name="atlasAsset" value="OBJECTS"/>
  </properties>
 </tile>
 <tile id="36">
  <properties>
   <property name="atlasAsset" value="OBJECTS"/>
  </properties>
 </tile>
 <tile id="37">
  <properties>
   <property name="atlasAsset" value="OBJECTS"/>
  </properties>
 </tile>
 <tile id="38">
  <properties>
   <property name="atlasAsset" value="OBJECTS"/>
  </properties>
 </tile>
 <tile id="39">
  <properties>
   <property name="atlasAsset" value="OBJECTS"/>
  </properties>
 </tile>
 <tile id="45" type="Prop">
  <properties>
   <property name="direction" value="DOWN"/>
   <property name="restitution" type="float" value="1"/>
   <property name="sensor" type="bool" value="true"/>
   <property name="speed" type="float" value="2"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="9" y="0" width="16" height="32"/>
  </objectgroup>
 </tile>
 <tile id="47" type="Prop">
  <properties>
   <property name="direction" value="UP"/>
   <property name="restitution" type="float" value="1"/>
   <property name="sensor" type="bool" value="true"/>
   <property name="speed" type="float" value="2"/>
  </properties>
 </tile>
 <tile id="48" type="Prop">
  <properties>
   <property name="restitution" type="float" value="1"/>
   <property name="seatIndex" type="int" value="0"/>
   <property name="sensor" type="bool" value="true"/>
   <property name="tableId" type="int" value="0"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="14" width="32" height="10"/>
  </objectgroup>
 </tile>
 <tile id="49" type="Prop">
  <properties>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="restitution" type="float" value="1"/>
   <property name="seatIndex" type="int" value="0"/>
   <property name="sensor" type="bool" value="true"/>
   <property name="tableId" type="int" value="0"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="8" y="14.125" width="16" height="10"/>
  </objectgroup>
 </tile>
 <tile id="61" type="Prop">
  <properties>
   <property name="direction" value="RIGHT"/>
   <property name="restitution" type="float" value="1"/>
   <property name="sensor" type="bool" value="true"/>
   <property name="speed" type="float" value="2"/>
  </properties>
  <objectgroup draworder="index" id="5">
   <object id="5" x="8.8909" y="0.0722837">
    <polygon points="0,0 0.144567,23.8536 8.02349,31.7326 23.1091,31.9277 23.1308,8.8909 18.3601,8.8909 16.047,6.50553 16.047,-0.0722837"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="62" type="Prop">
  <properties>
   <property name="direction" value="RIGHT"/>
   <property name="restitution" type="float" value="1"/>
   <property name="sensor" type="bool" value="true"/>
   <property name="speed" type="float" value="2"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="9" width="32" height="23"/>
  </objectgroup>
 </tile>
 <tile id="63" type="Prop">
  <properties>
   <property name="direction" value="UP"/>
   <property name="restitution" type="float" value="1"/>
   <property name="sensor" type="bool" value="true"/>
   <property name="speed" type="float" value="2"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="32">
    <polygon points="0,0 13.0111,0.0216851 20.9623,-8.21866 21.902,-31.9277 7.0838,-31.9277 7.95121,-18.6998 3.9756,-15.5193 0,-15.6639"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="64" type="Prop">
  <properties>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="restitution" type="float" value="1"/>
   <property name="seats" type="int" value="2"/>
   <property name="sensor" type="bool" value="true"/>
   <property name="tableId" type="int" value="0"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="1" y="11.75" width="30" height="17.25"/>
  </objectgroup>
 </tile>
 <tile id="65" type="Prop">
  <properties>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="restitution" type="float" value="1"/>
   <property name="seats" type="int" value="2"/>
   <property name="sensor" type="bool" value="true"/>
   <property name="tableId" type="int" value="0"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="7" y="14.125" width="18" height="14.875"/>
  </objectgroup>
 </tile>
 <tile id="67" type="Prop">
  <properties>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="restitution" type="float" value="1"/>
   <property name="sensor" type="bool" value="true"/>
   <property name="z" type="int" value="3"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="10.5" y="25.875" width="11" height="6.125"/>
  </objectgroup>
 </tile>
 <tile id="68" type="Prop">
  <properties>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="restitution" type="float" value="1"/>
   <property name="sensor" type="bool" value="true"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="9.375" y="26.625" width="13.25" height="5.375"/>
  </objectgroup>
 </tile>
 <tile id="69" type="Prop">
  <properties>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="restitution" type="float" value="1"/>
   <property name="sensor" type="bool" value="true"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="8" y="21.25" width="16" height="10.75"/>
  </objectgroup>
 </tile>
 <tile id="70" type="Prop">
  <properties>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="restitution" type="float" value="1"/>
   <property name="sensor" type="bool" value="true"/>
  </properties>
 </tile>
 <tile id="71" type="Prop">
  <properties>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="restitution" type="float" value="1"/>
   <property name="sensor" type="bool" value="true"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="8" y="21.75" width="16" height="10.25"/>
  </objectgroup>
 </tile>
 <tile id="74" type="Prop">
  <properties>
   <property name="restitution" type="float" value="1"/>
   <property name="sensor" type="bool" value="true"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="16" width="32" height="16"/>
  </objectgroup>
 </tile>
 <tile id="75" type="Prop">
  <properties>
   <property name="restitution" type="float" value="1"/>
   <property name="sensor" type="bool" value="true"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="16" width="32" height="16"/>
  </objectgroup>
 </tile>
 <tile id="76" type="Prop">
  <properties>
   <property name="restitution" type="float" value="1"/>
   <property name="sensor" type="bool" value="true"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="16" width="32" height="16"/>
  </objectgroup>
 </tile>
 <tile id="77" type="Prop">
  <properties>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="restitution" type="float" value="1"/>
   <property name="sensor" type="bool" value="true"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="16" width="32" height="16"/>
  </objectgroup>
 </tile>
 <tile id="78" type="Prop">
  <properties>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="restitution" type="float" value="1"/>
   <property name="sensor" type="bool" value="true"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="16" width="32" height="16"/>
  </objectgroup>
 </tile>
 <tile id="85" type="Prop">
  <properties>
   <property name="restitution" type="float" value="1"/>
   <property name="sensor" type="bool" value="true"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="8" y="16" width="17" height="16"/>
  </objectgroup>
 </tile>
 <tile id="90" type="Prop">
  <properties>
   <property name="restitution" type="float" value="1"/>
   <property name="sensor" type="bool" value="true"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="17" height="32"/>
  </objectgroup>
 </tile>
 <tile id="91" type="Prop">
  <properties>
   <property name="restitution" type="float" value="1"/>
   <property name="sensor" type="bool" value="true"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="16" width="32" height="16"/>
  </objectgroup>
 </tile>
 <tile id="92" type="Prop">
  <properties>
   <property name="restitution" type="float" value="1"/>
   <property name="sensor" type="bool" value="true"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="16" width="32" height="16"/>
  </objectgroup>
 </tile>
 <tile id="93" type="Prop">
  <properties>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="restitution" type="float" value="1"/>
   <property name="sensor" type="bool" value="true"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="8" width="16" height="24"/>
  </objectgroup>
 </tile>
 <tile id="106" type="Prop">
  <properties>
   <property name="restitution" type="float" value="1"/>
   <property name="sensor" type="bool" value="true"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="16" width="32" height="16"/>
   <object id="2" x="0" y="0" width="16" height="16"/>
  </objectgroup>
 </tile>
 <tile id="107" type="Prop">
  <properties>
   <property name="restitution" type="float" value="1"/>
   <property name="sensor" type="bool" value="true"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="16" width="32" height="16"/>
  </objectgroup>
 </tile>
 <tile id="108" type="Prop">
  <properties>
   <property name="restitution" type="float" value="1"/>
   <property name="sensor" type="bool" value="true"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="16" width="32" height="16"/>
  </objectgroup>
 </tile>
 <tile id="109" type="Prop">
  <properties>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="restitution" type="float" value="1"/>
   <property name="sensor" type="bool" value="true"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="16" width="32" height="16"/>
   <object id="2" x="0" y="8" width="16" height="8"/>
  </objectgroup>
 </tile>
 <tile id="110" type="Prop">
  <properties>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="restitution" type="float" value="1"/>
   <property name="sensor" type="bool" value="true"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="16" width="32" height="16"/>
  </objectgroup>
 </tile>
 <tile id="123">
  <properties>
   <property name="atlasAsset" value="OBJECTS"/>
  </properties>
 </tile>
 <tile id="124">
  <properties>
   <property name="atlasAsset" value="OBJECTS"/>
  </properties>
 </tile>
 <tile id="125">
  <properties>
   <property name="atlasAsset" value="OBJECTS"/>
  </properties>
 </tile>
 <tile id="126">
  <properties>
   <property name="atlasAsset" value="OBJECTS"/>
  </properties>
 </tile>
 <tile id="139">
  <properties>
   <property name="atlasAsset" value="OBJECTS"/>
  </properties>
 </tile>
 <tile id="140">
  <properties>
   <property name="atlasAsset" value="OBJECTS"/>
  </properties>
 </tile>
 <tile id="141">
  <properties>
   <property name="atlasAsset" value="OBJECTS"/>
  </properties>
 </tile>
 <tile id="240">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="2"/>
   <property name="controller" type="bool" value="true"/>
   <property name="friction" type="float" value="1"/>
   <property name="speed" type="float" value="4"/>
  </properties>
 </tile>
</tileset>
