// Description: Java 25 implementation of an in-memory RAM CFInt schema.

/*
 *	io.github.msobkow.CFInt
 *
 *	Copyright (c) 2016-2026 Mark Stephen Sobkow
 *	
 *	Mark's Code Fractal 3.1 CFInt - Internet Essentials
 *	
 *	This file is part of Mark's Code Fractal CFInt.
 *	
 *	Mark's Code Fractal CFInt is available under dual commercial license from
 *	Mark Stephen Sobkow, or under the terms of the GNU Library General Public License,
 *	Version 3 or later.
 *	
 *	Mark's Code Fractal CFInt is free software: you can redistribute it and/or
 *	modify it under the terms of the GNU Library General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *	
 *	Mark's Code Fractal CFInt is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *	
 *	You should have received a copy of the GNU Library General Public License
 *	along with Mark's Code Fractal CFInt.  If not, see <https://www.gnu.org/licenses/>.
 *	
 *	If you wish to modify and use this code without publishing your changes in order to
 *	tie it to proprietary code, please contact Mark Stephen Sobkow
 *	for a commercial license at mark.sobkow@gmail.com
 *	
 */

package io.github.msobkow.v3_1.cfint.cfintram;

import java.lang.reflect.*;
import java.net.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import io.github.msobkow.v3_1.cflib.*;
import io.github.msobkow.v3_1.cflib.dbutil.*;

import io.github.msobkow.v3_1.cfsec.cfsec.*;
import io.github.msobkow.v3_1.cfint.cfint.*;
import io.github.msobkow.v3_1.cfsec.cfsecobj.*;
import io.github.msobkow.v3_1.cfint.cfintobj.*;
import io.github.msobkow.v3_1.cfsec.cfsec.buff.*;
import io.github.msobkow.v3_1.cfint.cfint.buff.*;
import io.github.msobkow.v3_1.cfint.cfintsaxloader.*;

public class CFIntRamSchema
	extends CFIntBuffSchema
	implements ICFIntSchema
{
	protected int nextMimeTypeIdGenValue = 1;
	protected int nextURLProtocolIdGenValue = 1;


	public CFIntRamSchema() {
		super();
		tableLicense = new CFIntRamLicenseTable( this );
		tableMajorVersion = new CFIntRamMajorVersionTable( this );
		tableMimeType = new CFIntRamMimeTypeTable( this );
		tableMinorVersion = new CFIntRamMinorVersionTable( this );
		tableSubProject = new CFIntRamSubProjectTable( this );
		tableTld = new CFIntRamTldTable( this );
		tableTopDomain = new CFIntRamTopDomainTable( this );
		tableTopProject = new CFIntRamTopProjectTable( this );
		tableURLProtocol = new CFIntRamURLProtocolTable( this );
	}

	public ICFIntSchema newSchema() {
		throw new CFLibMustOverrideException( getClass(), "newSchema" );
	}

	public int nextMimeTypeIdGen() {
		int next = nextMimeTypeIdGenValue++;
		return( next );
	}

	public int nextURLProtocolIdGen() {
		int next = nextURLProtocolIdGenValue++;
		return( next );
	}

	public String fileImport( CFSecAuthorization Authorization,
		String fileName,
		String fileContent )
	{
		final String S_ProcName = "fileImport";
		if( ( fileName == null ) || ( fileName.length() <= 0 ) ) {
			throw new CFLibNullArgumentException( getClass(),
				S_ProcName,
				1,
				"fileName" );
		}
		if( ( fileContent == null ) || ( fileContent.length() <= 0 ) ) {
			throw new CFLibNullArgumentException( getClass(),
				S_ProcName,
				2,
				"fileContent" );
		}

		CFIntSaxLoader saxLoader = new CFIntSaxLoader();
		ICFIntSchemaObj schemaObj = new CFIntSchemaObj();
		schemaObj.setCFIntBackingStore( this );
		saxLoader.setSchemaObj( schemaObj );
		ICFSecClusterObj useCluster = schemaObj.getClusterTableObj().readClusterByIdIdx( Authorization.getSecClusterId() );
		ICFSecTenantObj useTenant = schemaObj.getTenantTableObj().readTenantByIdIdx( Authorization.getSecTenantId() );
		CFLibCachedMessageLog runlog = new CFLibCachedMessageLog();
		saxLoader.setLog( runlog );
		saxLoader.setUseCluster( useCluster );
		saxLoader.setUseTenant( useTenant );
		saxLoader.parseStringContents( fileContent );
		String logFileContent = runlog.getCacheContents();
		if( logFileContent == null ) {
			logFileContent = "";
		}

		return( logFileContent );
	}
}
