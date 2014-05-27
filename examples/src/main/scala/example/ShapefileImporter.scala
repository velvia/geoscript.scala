package org.geoscript.example

import com.vividsolutions.jts.geom.Geometry
import org.geoscript.feature._
import org.geoscript.feature.schemaBuilder._
import org.geoscript.layer._
import org.geoscript.projection._
import org.geoscript.workspace._
import org.geotools.geojson.geom.GeometryJSON

/**
 * Code to read in a shapefile, reproject to WGS84,
 * and output each feature in a different format
 */
object ShapefileImporter extends App {
  import collection.JavaConverters._

  if (args.size < 1) {
    println(
""" |Usage: ShapefileImporter <source shapefile>
    |Reprojects the source shapefile to WGS84 and spits out each field
    |""".stripMargin)
    System.exit(0)
  }

  val geoJsonWriter = new GeometryJSON()

  // Read in the shapefile and print out the original projection
  val shp = Shapefile(args(0))
  println(s"Ingested Shapefile with ${shp.count} features and projection\n${shp.schema.getCoordinateReferenceSystem}")

  // destination projection object
  val Some(proj) = lookupEPSG("EPSG:4326")
  val dstSchema = reproject(shp.schema, proj)

  val attrNames = dstSchema.getDescriptors.asScala.map(_.getName)

  for (feature <- shp.features) {
    val reprojected = reproject(feature, proj)
    reprojected.getAttributes.asScala.zip(attrNames).foreach { case (attr, name) =>
      attr match {
        case g: Geometry =>   println(s"${name}: ${geoJsonWriter.toString(g)}")
        case x           =>   println(s"${name}: ${x}")
      }
    }
  }
}