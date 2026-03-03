<?xml version="1.0" encoding="UTF-8"?>
<tileset version="1.10" tiledversion="1.11.2" name="Player" tilewidth="32" tileheight="32" tilecount="18" columns="9">
 <image source="Player.png" width="288" height="64"/>
 <tile id="0" type="GameObject">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="camFollow" type="bool" value="true"/>
   <property name="controller" type="bool" value="true"/>
   <property name="friction" type="float" value="1"/>
   <property name="speed" type="float" value="4"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="9.63636" y="24" width="12" height="6.72727"/>
  </objectgroup>
  <animation>
   <frame tileid="0" duration="200"/>
   <frame tileid="1" duration="200"/>
   <frame tileid="2" duration="200"/>
  </animation>
 </tile>
 <tile id="1">
  <properties>
   <property name="controller" type="bool" value="true"/>
   <property name="friction" type="float" value="1"/>
   <property name="speed" type="float" value="4"/>
  </properties>
 </tile>
 <tile id="2">
  <properties>
   <property name="controller" type="bool" value="true"/>
   <property name="friction" type="float" value="1"/>
   <property name="speed" type="float" value="4"/>
  </properties>
 </tile>
 <tile id="3">
  <properties>
   <property name="controller" type="bool" value="true"/>
   <property name="friction" type="float" value="1"/>
   <property name="speed" type="float" value="4"/>
  </properties>
 </tile>
 <tile id="4">
  <properties>
   <property name="controller" type="bool" value="true"/>
   <property name="friction" type="float" value="1"/>
   <property name="speed" type="float" value="4"/>
  </properties>
 </tile>
 <tile id="5">
  <properties>
   <property name="controller" type="bool" value="true"/>
   <property name="friction" type="float" value="1"/>
   <property name="speed" type="float" value="4"/>
  </properties>
 </tile>
 <tile id="6">
  <properties>
   <property name="controller" type="bool" value="true"/>
   <property name="friction" type="float" value="1"/>
   <property name="speed" type="float" value="4"/>
  </properties>
 </tile>
 <tile id="7">
  <properties>
   <property name="controller" type="bool" value="true"/>
   <property name="friction" type="float" value="1"/>
   <property name="speed" type="float" value="4"/>
  </properties>
 </tile>
 <tile id="8">
  <properties>
   <property name="controller" type="bool" value="true"/>
   <property name="friction" type="float" value="1"/>
   <property name="speed" type="float" value="4"/>
  </properties>
 </tile>
</tileset>
