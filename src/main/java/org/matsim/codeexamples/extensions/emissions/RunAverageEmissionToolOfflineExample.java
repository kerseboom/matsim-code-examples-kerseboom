/* *********************************************************************** *
 * project: org.matsim.*
 * RunEmissionToolOffline.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package org.matsim.codeexamples.extensions.emissions;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.emissions.EmissionModule;
import org.matsim.contrib.emissions.EmissionUtils;
import org.matsim.contrib.emissions.HbefaVehicleCategory;
import org.matsim.contrib.emissions.example.CreateEmissionConfig;
import org.matsim.contrib.emissions.utils.EmissionsConfigGroup;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Injector;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.events.algorithms.EventWriterXML;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.vehicles.MatsimVehicleWriter;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;


/**
 *
 * Use the config file as created by the
 * {@link CreateEmissionConfig CreateEmissionConfig} to calculate
 * emissions based on the link leave events of an events file. Resulting emission events are written into an event file.
 *
 * @author benjamin, julia
 */
public final class RunAverageEmissionToolOfflineExample{

	private static final String eventsFile =  "./scenarios/sampleScenario/5.events.xml.gz";

	/* package, for test */ static final String emissionEventOutputFileName = "5.emission.events.offline.xml.gz";

	// =======================================================================================================

	public static void main (String[] args){
		// see testcase for an example
		Config config ;
		if ( args==null || args.length==0 || args[0]==null ) {
			config = ConfigUtils.loadConfig( "./scenarios/sampleScenario/testv2_Vehv2/config_average.xml" );
		} else {
			config = ConfigUtils.loadConfig( args );
		}

		config.controler().setOutputDirectory( "output/" );

		EmissionsConfigGroup emissionsConfig = ConfigUtils.addOrGetModule( config, EmissionsConfigGroup.class );

//		emissionsConfig.setAverageColdEmissionFactorsFile( "../sample_EFA_ColdStart_vehcat_2005average.txt" );
//		emissionsConfig.setAverageWarmEmissionFactorsFile( "../sample_EFA_HOT_vehcat_2005average.txt" );

		emissionsConfig.setAverageColdEmissionFactorsFile( "../sample_41_EFA_ColdStart_vehcat_2020average.txt" );
		emissionsConfig.setAverageWarmEmissionFactorsFile( "../sample_41_EFA_HOT_vehcat_2020average.txt" );

		emissionsConfig.setDetailedVsAverageLookupBehavior( EmissionsConfigGroup.DetailedVsAverageLookupBehavior.directlyTryAverageTable );

		emissionsConfig.setHbefaRoadTypeSource( EmissionsConfigGroup.HbefaRoadTypeSource.fromLinkAttributes );

		emissionsConfig.setNonScenarioVehicles( EmissionsConfigGroup.NonScenarioVehicles.abort );

		// ---

		Scenario scenario = ScenarioUtils.loadScenario( config ) ;

//		for( Link link : scenario.getNetwork().getLinks().values() ){
//			if ( true ) {
//				EmissionUtils.setHbefaRoadType( link, "URB/Local/50" );
//			}
//		}
//
//		for( VehicleType vehicleType : scenario.getVehicles().getVehicleTypes().values() ){
//			if ( true ){
//				VehicleUtils.setHbefaVehicleCategory( vehicleType.getEngineInformation(), HbefaVehicleCategory.PASSENGER_CAR.toString() );
//				VehicleUtils.setHbefaTechnology( vehicleType.getEngineInformation(), "average" );
//				VehicleUtils.setHbefaEmissionsConcept( vehicleType.getEngineInformation(), "average" );
//				VehicleUtils.setHbefaSizeClass( vehicleType.getEngineInformation(), "average");
//			}
//		}

		// ---

		EventsManager eventsManager = EventsUtils.createEventsManager();

		AbstractModule module = new AbstractModule(){
			@Override
			public void install(){
				bind( Scenario.class ).toInstance( scenario );
				bind( EventsManager.class ).toInstance( eventsManager );
				bind( EmissionModule.class ) ;
			}
		};;

		com.google.inject.Injector injector = Injector.createInjector( config, module );

		EmissionModule emissionModule = injector.getInstance(EmissionModule.class);

		// ---

		final String outputDirectory = scenario.getConfig().controler().getOutputDirectory();
		EventWriterXML emissionEventWriter = new EventWriterXML( outputDirectory + emissionEventOutputFileName );
		emissionModule.getEmissionEventsManager().addHandler(emissionEventWriter);

		MatsimEventsReader matsimEventsReader = new MatsimEventsReader(eventsManager);
		matsimEventsReader.readFile(eventsFile);

		emissionEventWriter.closeFile();

		new MatsimVehicleWriter( scenario.getVehicles() ).writeFile( outputDirectory + "vehicles.xml.gz" );
		NetworkUtils.writeNetwork( scenario.getNetwork(), outputDirectory + "network.xml.gz" );

	}

}
