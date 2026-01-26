
// Description: Java 25 in-memory RAM DbIO implementation for SubProject.

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

import java.math.*;
import java.sql.*;
import java.text.*;
import java.time.*;
import java.util.*;
import org.apache.commons.codec.binary.Base64;
import io.github.msobkow.v3_1.cflib.*;
import io.github.msobkow.v3_1.cflib.dbutil.*;

import io.github.msobkow.v3_1.cfsec.cfsec.*;
import io.github.msobkow.v3_1.cfint.cfint.*;
import io.github.msobkow.v3_1.cfsec.cfsec.buff.*;
import io.github.msobkow.v3_1.cfint.cfint.buff.*;
import io.github.msobkow.v3_1.cfsec.cfsecobj.*;
import io.github.msobkow.v3_1.cfint.cfintobj.*;

/*
 *	CFIntRamSubProjectTable in-memory RAM DbIO implementation
 *	for SubProject.
 */
public class CFIntRamSubProjectTable
	implements ICFIntSubProjectTable
{
	private ICFIntSchema schema;
	private Map< CFLibDbKeyHash256,
				CFIntBuffSubProject > dictByPKey
		= new HashMap< CFLibDbKeyHash256,
				CFIntBuffSubProject >();
	private Map< CFIntBuffSubProjectByTenantIdxKey,
				Map< CFLibDbKeyHash256,
					CFIntBuffSubProject >> dictByTenantIdx
		= new HashMap< CFIntBuffSubProjectByTenantIdxKey,
				Map< CFLibDbKeyHash256,
					CFIntBuffSubProject >>();
	private Map< CFIntBuffSubProjectByTopProjectIdxKey,
				Map< CFLibDbKeyHash256,
					CFIntBuffSubProject >> dictByTopProjectIdx
		= new HashMap< CFIntBuffSubProjectByTopProjectIdxKey,
				Map< CFLibDbKeyHash256,
					CFIntBuffSubProject >>();
	private Map< CFIntBuffSubProjectByNameIdxKey,
			CFIntBuffSubProject > dictByNameIdx
		= new HashMap< CFIntBuffSubProjectByNameIdxKey,
			CFIntBuffSubProject >();

	public CFIntRamSubProjectTable( ICFIntSchema argSchema ) {
		schema = argSchema;
	}

	public ICFIntSubProject createSubProject( ICFSecAuthorization Authorization,
		ICFIntSubProject Buff )
	{
		final String S_ProcName = "createSubProject";
		CFLibDbKeyHash256 pkey;
		pkey = schema.nextSubProjectIdGen();
		Buff.setRequiredId( pkey );
		CFIntBuffSubProjectByTenantIdxKey keyTenantIdx = (CFIntBuffSubProjectByTenantIdxKey)schema.getFactorySubProject().newByTenantIdxKey();
		keyTenantIdx.setRequiredTenantId( Buff.getRequiredTenantId() );

		CFIntBuffSubProjectByTopProjectIdxKey keyTopProjectIdx = (CFIntBuffSubProjectByTopProjectIdxKey)schema.getFactorySubProject().newByTopProjectIdxKey();
		keyTopProjectIdx.setRequiredTopProjectId( Buff.getRequiredTopProjectId() );

		CFIntBuffSubProjectByNameIdxKey keyNameIdx = (CFIntBuffSubProjectByNameIdxKey)schema.getFactorySubProject().newByNameIdxKey();
		keyNameIdx.setRequiredTopProjectId( Buff.getRequiredTopProjectId() );
		keyNameIdx.setRequiredName( Buff.getRequiredName() );

		// Validate unique indexes

		if( dictByPKey.containsKey( pkey ) ) {
			throw new CFLibPrimaryKeyNotNewException( getClass(), S_ProcName, pkey );
		}

		if( dictByNameIdx.containsKey( keyNameIdx ) ) {
			throw new CFLibUniqueIndexViolationException( getClass(),
				S_ProcName,
				"SubProjectNameIdx",
				"SubProjectNameIdx",
				keyNameIdx );
		}

		// Validate foreign keys

		{
			boolean allNull = true;
			allNull = false;
			if( ! allNull ) {
				if( null == schema.getTableTenant().readDerivedByIdIdx( Authorization,
						Buff.getRequiredTenantId() ) )
				{
					throw new CFLibUnresolvedRelationException( getClass(),
						S_ProcName,
						"Owner",
						"Tenant",
						"Tenant",
						null );
				}
			}
		}

		{
			boolean allNull = true;
			allNull = false;
			if( ! allNull ) {
				if( null == schema.getTableTopProject().readDerivedByIdIdx( Authorization,
						Buff.getRequiredTopProjectId() ) )
				{
					throw new CFLibUnresolvedRelationException( getClass(),
						S_ProcName,
						"Container",
						"ParentTopProject",
						"TopProject",
						null );
				}
			}
		}

		// Proceed with adding the new record

		dictByPKey.put( pkey, Buff );

		Map< CFLibDbKeyHash256, CFIntBuffSubProject > subdictTenantIdx;
		if( dictByTenantIdx.containsKey( keyTenantIdx ) ) {
			subdictTenantIdx = dictByTenantIdx.get( keyTenantIdx );
		}
		else {
			subdictTenantIdx = new HashMap< CFLibDbKeyHash256, CFIntBuffSubProject >();
			dictByTenantIdx.put( keyTenantIdx, subdictTenantIdx );
		}
		subdictTenantIdx.put( pkey, Buff );

		Map< CFLibDbKeyHash256, CFIntBuffSubProject > subdictTopProjectIdx;
		if( dictByTopProjectIdx.containsKey( keyTopProjectIdx ) ) {
			subdictTopProjectIdx = dictByTopProjectIdx.get( keyTopProjectIdx );
		}
		else {
			subdictTopProjectIdx = new HashMap< CFLibDbKeyHash256, CFIntBuffSubProject >();
			dictByTopProjectIdx.put( keyTopProjectIdx, subdictTopProjectIdx );
		}
		subdictTopProjectIdx.put( pkey, Buff );

		dictByNameIdx.put( keyNameIdx, Buff );

		return( Buff );
	}

	public ICFIntSubProject readDerived( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 PKey )
	{
		final String S_ProcName = "CFIntRamSubProject.readDerived";
		ICFIntSubProject buff;
		if( dictByPKey.containsKey( PKey ) ) {
			buff = dictByPKey.get( PKey );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	public ICFIntSubProject lockDerived( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 PKey )
	{
		final String S_ProcName = "CFIntRamSubProject.readDerived";
		ICFIntSubProject buff;
		if( dictByPKey.containsKey( PKey ) ) {
			buff = dictByPKey.get( PKey );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	public ICFIntSubProject[] readAllDerived( ICFSecAuthorization Authorization ) {
		final String S_ProcName = "CFIntRamSubProject.readAllDerived";
		ICFIntSubProject[] retList = new ICFIntSubProject[ dictByPKey.values().size() ];
		Iterator< ICFIntSubProject > iter = dictByPKey.values().iterator();
		int idx = 0;
		while( iter.hasNext() ) {
			retList[ idx++ ] = iter.next();
		}
		return( retList );
	}

	public ICFIntSubProject[] readDerivedByTenantIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 TenantId )
	{
		final String S_ProcName = "CFIntRamSubProject.readDerivedByTenantIdx";
		CFIntBuffSubProjectByTenantIdxKey key = (CFIntBuffSubProjectByTenantIdxKey)schema.getFactorySubProject().newByTenantIdxKey();
		key.setRequiredTenantId( TenantId );

		ICFIntSubProject[] recArray;
		if( dictByTenantIdx.containsKey( key ) ) {
			Map< CFLibDbKeyHash256, CFIntBuffSubProject > subdictTenantIdx
				= dictByTenantIdx.get( key );
			recArray = new ICFIntSubProject[ subdictTenantIdx.size() ];
			Iterator< ICFIntSubProject > iter = subdictTenantIdx.values().iterator();
			int idx = 0;
			while( iter.hasNext() ) {
				recArray[ idx++ ] = iter.next();
			}
		}
		else {
			Map< CFLibDbKeyHash256, CFIntBuffSubProject > subdictTenantIdx
				= new HashMap< CFLibDbKeyHash256, CFIntBuffSubProject >();
			dictByTenantIdx.put( key, subdictTenantIdx );
			recArray = new ICFIntSubProject[0];
		}
		return( recArray );
	}

	public ICFIntSubProject[] readDerivedByTopProjectIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 TopProjectId )
	{
		final String S_ProcName = "CFIntRamSubProject.readDerivedByTopProjectIdx";
		CFIntBuffSubProjectByTopProjectIdxKey key = (CFIntBuffSubProjectByTopProjectIdxKey)schema.getFactorySubProject().newByTopProjectIdxKey();
		key.setRequiredTopProjectId( TopProjectId );

		ICFIntSubProject[] recArray;
		if( dictByTopProjectIdx.containsKey( key ) ) {
			Map< CFLibDbKeyHash256, CFIntBuffSubProject > subdictTopProjectIdx
				= dictByTopProjectIdx.get( key );
			recArray = new ICFIntSubProject[ subdictTopProjectIdx.size() ];
			Iterator< ICFIntSubProject > iter = subdictTopProjectIdx.values().iterator();
			int idx = 0;
			while( iter.hasNext() ) {
				recArray[ idx++ ] = iter.next();
			}
		}
		else {
			Map< CFLibDbKeyHash256, CFIntBuffSubProject > subdictTopProjectIdx
				= new HashMap< CFLibDbKeyHash256, CFIntBuffSubProject >();
			dictByTopProjectIdx.put( key, subdictTopProjectIdx );
			recArray = new ICFIntSubProject[0];
		}
		return( recArray );
	}

	public ICFIntSubProject readDerivedByNameIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 TopProjectId,
		String Name )
	{
		final String S_ProcName = "CFIntRamSubProject.readDerivedByNameIdx";
		CFIntBuffSubProjectByNameIdxKey key = (CFIntBuffSubProjectByNameIdxKey)schema.getFactorySubProject().newByNameIdxKey();
		key.setRequiredTopProjectId( TopProjectId );
		key.setRequiredName( Name );

		ICFIntSubProject buff;
		if( dictByNameIdx.containsKey( key ) ) {
			buff = dictByNameIdx.get( key );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	public ICFIntSubProject readDerivedByIdIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 Id )
	{
		final String S_ProcName = "CFIntRamSubProject.readDerivedByIdIdx() ";
		ICFIntSubProject buff;
		if( dictByPKey.containsKey( Id ) ) {
			buff = dictByPKey.get( Id );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	public ICFIntSubProject readBuff( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 PKey )
	{
		final String S_ProcName = "CFIntRamSubProject.readBuff";
		ICFIntSubProject buff = readDerived( Authorization, PKey );
		if( ( buff != null ) && ( buff.getClassCode() != ICFIntSubProject.CLASS_CODE ) ) {
			buff = null;
		}
		return( buff );
	}

	public ICFIntSubProject lockBuff( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 PKey )
	{
		final String S_ProcName = "lockBuff";
		ICFIntSubProject buff = readDerived( Authorization, PKey );
		if( ( buff != null ) && ( buff.getClassCode() != ICFIntSubProject.CLASS_CODE ) ) {
			buff = null;
		}
		return( buff );
	}

	public ICFIntSubProject[] readAllBuff( ICFSecAuthorization Authorization )
	{
		final String S_ProcName = "CFIntRamSubProject.readAllBuff";
		ICFIntSubProject buff;
		ArrayList<ICFIntSubProject> filteredList = new ArrayList<ICFIntSubProject>();
		ICFIntSubProject[] buffList = readAllDerived( Authorization );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && ( buff.getClassCode() == ICFIntSubProject.CLASS_CODE ) ) {
				filteredList.add( buff );
			}
		}
		return( filteredList.toArray( new ICFIntSubProject[0] ) );
	}

	public ICFIntSubProject readBuffByIdIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 Id )
	{
		final String S_ProcName = "CFIntRamSubProject.readBuffByIdIdx() ";
		ICFIntSubProject buff = readDerivedByIdIdx( Authorization,
			Id );
		if( ( buff != null ) && ( buff.getClassCode() == ICFIntSubProject.CLASS_CODE ) ) {
			return( (ICFIntSubProject)buff );
		}
		else {
			return( null );
		}
	}

	public ICFIntSubProject[] readBuffByTenantIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 TenantId )
	{
		final String S_ProcName = "CFIntRamSubProject.readBuffByTenantIdx() ";
		ICFIntSubProject buff;
		ArrayList<ICFIntSubProject> filteredList = new ArrayList<ICFIntSubProject>();
		ICFIntSubProject[] buffList = readDerivedByTenantIdx( Authorization,
			TenantId );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && ( buff.getClassCode() == ICFIntSubProject.CLASS_CODE ) ) {
				filteredList.add( (ICFIntSubProject)buff );
			}
		}
		return( filteredList.toArray( new ICFIntSubProject[0] ) );
	}

	public ICFIntSubProject[] readBuffByTopProjectIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 TopProjectId )
	{
		final String S_ProcName = "CFIntRamSubProject.readBuffByTopProjectIdx() ";
		ICFIntSubProject buff;
		ArrayList<ICFIntSubProject> filteredList = new ArrayList<ICFIntSubProject>();
		ICFIntSubProject[] buffList = readDerivedByTopProjectIdx( Authorization,
			TopProjectId );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && ( buff.getClassCode() == ICFIntSubProject.CLASS_CODE ) ) {
				filteredList.add( (ICFIntSubProject)buff );
			}
		}
		return( filteredList.toArray( new ICFIntSubProject[0] ) );
	}

	public ICFIntSubProject readBuffByNameIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 TopProjectId,
		String Name )
	{
		final String S_ProcName = "CFIntRamSubProject.readBuffByNameIdx() ";
		ICFIntSubProject buff = readDerivedByNameIdx( Authorization,
			TopProjectId,
			Name );
		if( ( buff != null ) && ( buff.getClassCode() == ICFIntSubProject.CLASS_CODE ) ) {
			return( (ICFIntSubProject)buff );
		}
		else {
			return( null );
		}
	}

	public ICFIntSubProject updateSubProject( ICFSecAuthorization Authorization,
		ICFIntSubProject Buff )
	{
		CFLibDbKeyHash256 pkey = Buff.getPKey();
		ICFIntSubProject existing = dictByPKey.get( pkey );
		if( existing == null ) {
			throw new CFLibStaleCacheDetectedException( getClass(),
				"updateSubProject",
				"Existing record not found",
				"SubProject",
				pkey );
		}
		if( existing.getRequiredRevision() != Buff.getRequiredRevision() ) {
			throw new CFLibCollisionDetectedException( getClass(),
				"updateSubProject",
				pkey );
		}
		Buff.setRequiredRevision( Buff.getRequiredRevision() + 1 );
		CFIntBuffSubProjectByTenantIdxKey existingKeyTenantIdx = (CFIntBuffSubProjectByTenantIdxKey)schema.getFactorySubProject().newByTenantIdxKey();
		existingKeyTenantIdx.setRequiredTenantId( existing.getRequiredTenantId() );

		CFIntBuffSubProjectByTenantIdxKey newKeyTenantIdx = (CFIntBuffSubProjectByTenantIdxKey)schema.getFactorySubProject().newByTenantIdxKey();
		newKeyTenantIdx.setRequiredTenantId( Buff.getRequiredTenantId() );

		CFIntBuffSubProjectByTopProjectIdxKey existingKeyTopProjectIdx = (CFIntBuffSubProjectByTopProjectIdxKey)schema.getFactorySubProject().newByTopProjectIdxKey();
		existingKeyTopProjectIdx.setRequiredTopProjectId( existing.getRequiredTopProjectId() );

		CFIntBuffSubProjectByTopProjectIdxKey newKeyTopProjectIdx = (CFIntBuffSubProjectByTopProjectIdxKey)schema.getFactorySubProject().newByTopProjectIdxKey();
		newKeyTopProjectIdx.setRequiredTopProjectId( Buff.getRequiredTopProjectId() );

		CFIntBuffSubProjectByNameIdxKey existingKeyNameIdx = (CFIntBuffSubProjectByNameIdxKey)schema.getFactorySubProject().newByNameIdxKey();
		existingKeyNameIdx.setRequiredTopProjectId( existing.getRequiredTopProjectId() );
		existingKeyNameIdx.setRequiredName( existing.getRequiredName() );

		CFIntBuffSubProjectByNameIdxKey newKeyNameIdx = (CFIntBuffSubProjectByNameIdxKey)schema.getFactorySubProject().newByNameIdxKey();
		newKeyNameIdx.setRequiredTopProjectId( Buff.getRequiredTopProjectId() );
		newKeyNameIdx.setRequiredName( Buff.getRequiredName() );

		// Check unique indexes

		if( ! existingKeyNameIdx.equals( newKeyNameIdx ) ) {
			if( dictByNameIdx.containsKey( newKeyNameIdx ) ) {
				throw new CFLibUniqueIndexViolationException( getClass(),
					"updateSubProject",
					"SubProjectNameIdx",
					"SubProjectNameIdx",
					newKeyNameIdx );
			}
		}

		// Validate foreign keys

		{
			boolean allNull = true;

			if( allNull ) {
				if( null == schema.getTableTenant().readDerivedByIdIdx( Authorization,
						Buff.getRequiredTenantId() ) )
				{
					throw new CFLibUnresolvedRelationException( getClass(),
						"updateSubProject",
						"Owner",
						"Tenant",
						"Tenant",
						null );
				}
			}
		}

		{
			boolean allNull = true;

			if( allNull ) {
				if( null == schema.getTableTopProject().readDerivedByIdIdx( Authorization,
						Buff.getRequiredTopProjectId() ) )
				{
					throw new CFLibUnresolvedRelationException( getClass(),
						"updateSubProject",
						"Container",
						"ParentTopProject",
						"TopProject",
						null );
				}
			}
		}

		// Update is valid

		Map< CFLibDbKeyHash256, CFIntBuffSubProject > subdict;

		dictByPKey.remove( pkey );
		dictByPKey.put( pkey, Buff );

		subdict = dictByTenantIdx.get( existingKeyTenantIdx );
		if( subdict != null ) {
			subdict.remove( pkey );
		}
		if( dictByTenantIdx.containsKey( newKeyTenantIdx ) ) {
			subdict = dictByTenantIdx.get( newKeyTenantIdx );
		}
		else {
			subdict = new HashMap< CFLibDbKeyHash256, CFIntBuffSubProject >();
			dictByTenantIdx.put( newKeyTenantIdx, subdict );
		}
		subdict.put( pkey, Buff );

		subdict = dictByTopProjectIdx.get( existingKeyTopProjectIdx );
		if( subdict != null ) {
			subdict.remove( pkey );
		}
		if( dictByTopProjectIdx.containsKey( newKeyTopProjectIdx ) ) {
			subdict = dictByTopProjectIdx.get( newKeyTopProjectIdx );
		}
		else {
			subdict = new HashMap< CFLibDbKeyHash256, CFIntBuffSubProject >();
			dictByTopProjectIdx.put( newKeyTopProjectIdx, subdict );
		}
		subdict.put( pkey, Buff );

		dictByNameIdx.remove( existingKeyNameIdx );
		dictByNameIdx.put( newKeyNameIdx, Buff );

		return(Buff);
	}

	public void deleteSubProject( ICFSecAuthorization Authorization,
		ICFIntSubProject Buff )
	{
		final String S_ProcName = "CFIntRamSubProjectTable.deleteSubProject() ";
		String classCode;
		CFLibDbKeyHash256 pkey = schema.getFactorySubProject().newPKey();
		pkey.setRequiredId( Buff.getRequiredId() );
		ICFIntSubProject existing = dictByPKey.get( pkey );
		if( existing == null ) {
			return;
		}
		if( existing.getRequiredRevision() != Buff.getRequiredRevision() )
		{
			throw new CFLibCollisionDetectedException( getClass(),
				"deleteSubProject",
				pkey );
		}
					schema.getTableMajorVersion().deleteMajorVersionBySubProjectIdx( Authorization,
						existing.getRequiredId() );
		CFIntBuffSubProjectByTenantIdxKey keyTenantIdx = (CFIntBuffSubProjectByTenantIdxKey)schema.getFactorySubProject().newByTenantIdxKey();
		keyTenantIdx.setRequiredTenantId( existing.getRequiredTenantId() );

		CFIntBuffSubProjectByTopProjectIdxKey keyTopProjectIdx = (CFIntBuffSubProjectByTopProjectIdxKey)schema.getFactorySubProject().newByTopProjectIdxKey();
		keyTopProjectIdx.setRequiredTopProjectId( existing.getRequiredTopProjectId() );

		CFIntBuffSubProjectByNameIdxKey keyNameIdx = (CFIntBuffSubProjectByNameIdxKey)schema.getFactorySubProject().newByNameIdxKey();
		keyNameIdx.setRequiredTopProjectId( existing.getRequiredTopProjectId() );
		keyNameIdx.setRequiredName( existing.getRequiredName() );

		// Validate reverse foreign keys

		// Delete is valid
		Map< CFLibDbKeyHash256, CFIntBuffSubProject > subdict;

		dictByPKey.remove( pkey );

		subdict = dictByTenantIdx.get( keyTenantIdx );
		subdict.remove( pkey );

		subdict = dictByTopProjectIdx.get( keyTopProjectIdx );
		subdict.remove( pkey );

		dictByNameIdx.remove( keyNameIdx );

	}
	public void deleteSubProjectByIdIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 argKey )
	{
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		ICFIntSubProject cur;
		LinkedList<ICFIntSubProject> matchSet = new LinkedList<ICFIntSubProject>();
		Iterator<ICFIntSubProject> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<ICFIntSubProject> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = schema.getTableSubProject().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() );
			deleteSubProject( Authorization, cur );
		}
	}

	public void deleteSubProjectByTenantIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 argTenantId )
	{
		CFIntBuffSubProjectByTenantIdxKey key = (CFIntBuffSubProjectByTenantIdxKey)schema.getFactorySubProject().newByTenantIdxKey();
		key.setRequiredTenantId( argTenantId );
		deleteSubProjectByTenantIdx( Authorization, key );
	}

	public void deleteSubProjectByTenantIdx( ICFSecAuthorization Authorization,
		ICFIntSubProjectByTenantIdxKey argKey )
	{
		ICFIntSubProject cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<ICFIntSubProject> matchSet = new LinkedList<ICFIntSubProject>();
		Iterator<ICFIntSubProject> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<ICFIntSubProject> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = schema.getTableSubProject().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() );
			deleteSubProject( Authorization, cur );
		}
	}

	public void deleteSubProjectByTopProjectIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 argTopProjectId )
	{
		CFIntBuffSubProjectByTopProjectIdxKey key = (CFIntBuffSubProjectByTopProjectIdxKey)schema.getFactorySubProject().newByTopProjectIdxKey();
		key.setRequiredTopProjectId( argTopProjectId );
		deleteSubProjectByTopProjectIdx( Authorization, key );
	}

	public void deleteSubProjectByTopProjectIdx( ICFSecAuthorization Authorization,
		ICFIntSubProjectByTopProjectIdxKey argKey )
	{
		ICFIntSubProject cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<ICFIntSubProject> matchSet = new LinkedList<ICFIntSubProject>();
		Iterator<ICFIntSubProject> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<ICFIntSubProject> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = schema.getTableSubProject().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() );
			deleteSubProject( Authorization, cur );
		}
	}

	public void deleteSubProjectByNameIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 argTopProjectId,
		String argName )
	{
		CFIntBuffSubProjectByNameIdxKey key = (CFIntBuffSubProjectByNameIdxKey)schema.getFactorySubProject().newByNameIdxKey();
		key.setRequiredTopProjectId( argTopProjectId );
		key.setRequiredName( argName );
		deleteSubProjectByNameIdx( Authorization, key );
	}

	public void deleteSubProjectByNameIdx( ICFSecAuthorization Authorization,
		ICFIntSubProjectByNameIdxKey argKey )
	{
		ICFIntSubProject cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<ICFIntSubProject> matchSet = new LinkedList<ICFIntSubProject>();
		Iterator<ICFIntSubProject> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<ICFIntSubProject> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = schema.getTableSubProject().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() );
			deleteSubProject( Authorization, cur );
		}
	}
}
