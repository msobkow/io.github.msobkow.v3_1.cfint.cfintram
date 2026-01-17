
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
import java.util.*;
import org.apache.commons.codec.binary.Base64;
import io.github.msobkow.v3_1.cflib.*;
import io.github.msobkow.v3_1.cflib.dbutil.*;

import io.github.msobkow.v3_1.cfsec.cfsec.*;
import io.github.msobkow.v3_1.cfint.cfint.*;
import io.github.msobkow.v3_1.cfint.cfintobj.*;
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
	private Map< CFIntSubProjectPKey,
				CFIntSubProjectBuff > dictByPKey
		= new HashMap< CFIntSubProjectPKey,
				CFIntSubProjectBuff >();
	private Map< CFIntSubProjectByTenantIdxKey,
				Map< CFIntSubProjectPKey,
					CFIntSubProjectBuff >> dictByTenantIdx
		= new HashMap< CFIntSubProjectByTenantIdxKey,
				Map< CFIntSubProjectPKey,
					CFIntSubProjectBuff >>();
	private Map< CFIntSubProjectByTopProjectIdxKey,
				Map< CFIntSubProjectPKey,
					CFIntSubProjectBuff >> dictByTopProjectIdx
		= new HashMap< CFIntSubProjectByTopProjectIdxKey,
				Map< CFIntSubProjectPKey,
					CFIntSubProjectBuff >>();
	private Map< CFIntSubProjectByNameIdxKey,
			CFIntSubProjectBuff > dictByNameIdx
		= new HashMap< CFIntSubProjectByNameIdxKey,
			CFIntSubProjectBuff >();

	public CFIntRamSubProjectTable( ICFIntSchema argSchema ) {
		schema = argSchema;
	}

	public void createSubProject( CFSecAuthorization Authorization,
		CFIntSubProjectBuff Buff )
	{
		final String S_ProcName = "createSubProject";
		CFIntSubProjectPKey pkey = schema.getFactorySubProject().newPKey();
		pkey.setRequiredId( schema.nextSubProjectIdGen() );
		Buff.setRequiredId( pkey.getRequiredId() );
		CFIntSubProjectByTenantIdxKey keyTenantIdx = schema.getFactorySubProject().newTenantIdxKey();
		keyTenantIdx.setRequiredTenantId( Buff.getRequiredTenantId() );

		CFIntSubProjectByTopProjectIdxKey keyTopProjectIdx = schema.getFactorySubProject().newTopProjectIdxKey();
		keyTopProjectIdx.setRequiredTopProjectId( Buff.getRequiredTopProjectId() );

		CFIntSubProjectByNameIdxKey keyNameIdx = schema.getFactorySubProject().newNameIdxKey();
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

		Map< CFIntSubProjectPKey, CFIntSubProjectBuff > subdictTenantIdx;
		if( dictByTenantIdx.containsKey( keyTenantIdx ) ) {
			subdictTenantIdx = dictByTenantIdx.get( keyTenantIdx );
		}
		else {
			subdictTenantIdx = new HashMap< CFIntSubProjectPKey, CFIntSubProjectBuff >();
			dictByTenantIdx.put( keyTenantIdx, subdictTenantIdx );
		}
		subdictTenantIdx.put( pkey, Buff );

		Map< CFIntSubProjectPKey, CFIntSubProjectBuff > subdictTopProjectIdx;
		if( dictByTopProjectIdx.containsKey( keyTopProjectIdx ) ) {
			subdictTopProjectIdx = dictByTopProjectIdx.get( keyTopProjectIdx );
		}
		else {
			subdictTopProjectIdx = new HashMap< CFIntSubProjectPKey, CFIntSubProjectBuff >();
			dictByTopProjectIdx.put( keyTopProjectIdx, subdictTopProjectIdx );
		}
		subdictTopProjectIdx.put( pkey, Buff );

		dictByNameIdx.put( keyNameIdx, Buff );

	}

	public CFIntSubProjectBuff readDerived( CFSecAuthorization Authorization,
		CFIntSubProjectPKey PKey )
	{
		final String S_ProcName = "CFIntRamSubProject.readDerived";
		CFIntSubProjectPKey key = schema.getFactorySubProject().newPKey();
		key.setRequiredId( PKey.getRequiredId() );
		CFIntSubProjectBuff buff;
		if( dictByPKey.containsKey( key ) ) {
			buff = dictByPKey.get( key );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	public CFIntSubProjectBuff lockDerived( CFSecAuthorization Authorization,
		CFIntSubProjectPKey PKey )
	{
		final String S_ProcName = "CFIntRamSubProject.readDerived";
		CFIntSubProjectPKey key = schema.getFactorySubProject().newPKey();
		key.setRequiredId( PKey.getRequiredId() );
		CFIntSubProjectBuff buff;
		if( dictByPKey.containsKey( key ) ) {
			buff = dictByPKey.get( key );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	public CFIntSubProjectBuff[] readAllDerived( CFSecAuthorization Authorization ) {
		final String S_ProcName = "CFIntRamSubProject.readAllDerived";
		CFIntSubProjectBuff[] retList = new CFIntSubProjectBuff[ dictByPKey.values().size() ];
		Iterator< CFIntSubProjectBuff > iter = dictByPKey.values().iterator();
		int idx = 0;
		while( iter.hasNext() ) {
			retList[ idx++ ] = iter.next();
		}
		return( retList );
	}

	public CFIntSubProjectBuff[] readDerivedByTenantIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 TenantId )
	{
		final String S_ProcName = "CFIntRamSubProject.readDerivedByTenantIdx";
		CFIntSubProjectByTenantIdxKey key = schema.getFactorySubProject().newTenantIdxKey();
		key.setRequiredTenantId( TenantId );

		CFIntSubProjectBuff[] recArray;
		if( dictByTenantIdx.containsKey( key ) ) {
			Map< CFIntSubProjectPKey, CFIntSubProjectBuff > subdictTenantIdx
				= dictByTenantIdx.get( key );
			recArray = new CFIntSubProjectBuff[ subdictTenantIdx.size() ];
			Iterator< CFIntSubProjectBuff > iter = subdictTenantIdx.values().iterator();
			int idx = 0;
			while( iter.hasNext() ) {
				recArray[ idx++ ] = iter.next();
			}
		}
		else {
			Map< CFIntSubProjectPKey, CFIntSubProjectBuff > subdictTenantIdx
				= new HashMap< CFIntSubProjectPKey, CFIntSubProjectBuff >();
			dictByTenantIdx.put( key, subdictTenantIdx );
			recArray = new CFIntSubProjectBuff[0];
		}
		return( recArray );
	}

	public CFIntSubProjectBuff[] readDerivedByTopProjectIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 TopProjectId )
	{
		final String S_ProcName = "CFIntRamSubProject.readDerivedByTopProjectIdx";
		CFIntSubProjectByTopProjectIdxKey key = schema.getFactorySubProject().newTopProjectIdxKey();
		key.setRequiredTopProjectId( TopProjectId );

		CFIntSubProjectBuff[] recArray;
		if( dictByTopProjectIdx.containsKey( key ) ) {
			Map< CFIntSubProjectPKey, CFIntSubProjectBuff > subdictTopProjectIdx
				= dictByTopProjectIdx.get( key );
			recArray = new CFIntSubProjectBuff[ subdictTopProjectIdx.size() ];
			Iterator< CFIntSubProjectBuff > iter = subdictTopProjectIdx.values().iterator();
			int idx = 0;
			while( iter.hasNext() ) {
				recArray[ idx++ ] = iter.next();
			}
		}
		else {
			Map< CFIntSubProjectPKey, CFIntSubProjectBuff > subdictTopProjectIdx
				= new HashMap< CFIntSubProjectPKey, CFIntSubProjectBuff >();
			dictByTopProjectIdx.put( key, subdictTopProjectIdx );
			recArray = new CFIntSubProjectBuff[0];
		}
		return( recArray );
	}

	public CFIntSubProjectBuff readDerivedByNameIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 TopProjectId,
		String Name )
	{
		final String S_ProcName = "CFIntRamSubProject.readDerivedByNameIdx";
		CFIntSubProjectByNameIdxKey key = schema.getFactorySubProject().newNameIdxKey();
		key.setRequiredTopProjectId( TopProjectId );
		key.setRequiredName( Name );

		CFIntSubProjectBuff buff;
		if( dictByNameIdx.containsKey( key ) ) {
			buff = dictByNameIdx.get( key );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	public CFIntSubProjectBuff readDerivedByIdIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 Id )
	{
		final String S_ProcName = "CFIntRamSubProject.readDerivedByIdIdx() ";
		CFIntSubProjectPKey key = schema.getFactorySubProject().newPKey();
		key.setRequiredId( Id );

		CFIntSubProjectBuff buff;
		if( dictByPKey.containsKey( key ) ) {
			buff = dictByPKey.get( key );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	public CFIntSubProjectBuff readBuff( CFSecAuthorization Authorization,
		CFIntSubProjectPKey PKey )
	{
		final String S_ProcName = "CFIntRamSubProject.readBuff";
		CFIntSubProjectBuff buff = readDerived( Authorization, PKey );
		if( ( buff != null ) && ( ! buff.getClassCode().equals( "a105" ) ) ) {
			buff = null;
		}
		return( buff );
	}

	public CFIntSubProjectBuff lockBuff( CFSecAuthorization Authorization,
		CFIntSubProjectPKey PKey )
	{
		final String S_ProcName = "lockBuff";
		CFIntSubProjectBuff buff = readDerived( Authorization, PKey );
		if( ( buff != null ) && ( ! buff.getClassCode().equals( "a105" ) ) ) {
			buff = null;
		}
		return( buff );
	}

	public CFIntSubProjectBuff[] readAllBuff( CFSecAuthorization Authorization )
	{
		final String S_ProcName = "CFIntRamSubProject.readAllBuff";
		CFIntSubProjectBuff buff;
		ArrayList<CFIntSubProjectBuff> filteredList = new ArrayList<CFIntSubProjectBuff>();
		CFIntSubProjectBuff[] buffList = readAllDerived( Authorization );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && buff.getClassCode().equals( "a105" ) ) {
				filteredList.add( buff );
			}
		}
		return( filteredList.toArray( new CFIntSubProjectBuff[0] ) );
	}

	public CFIntSubProjectBuff readBuffByIdIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 Id )
	{
		final String S_ProcName = "CFIntRamSubProject.readBuffByIdIdx() ";
		CFIntSubProjectBuff buff = readDerivedByIdIdx( Authorization,
			Id );
		if( ( buff != null ) && buff.getClassCode().equals( "a105" ) ) {
			return( (CFIntSubProjectBuff)buff );
		}
		else {
			return( null );
		}
	}

	public CFIntSubProjectBuff[] readBuffByTenantIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 TenantId )
	{
		final String S_ProcName = "CFIntRamSubProject.readBuffByTenantIdx() ";
		CFIntSubProjectBuff buff;
		ArrayList<CFIntSubProjectBuff> filteredList = new ArrayList<CFIntSubProjectBuff>();
		CFIntSubProjectBuff[] buffList = readDerivedByTenantIdx( Authorization,
			TenantId );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && buff.getClassCode().equals( "a105" ) ) {
				filteredList.add( (CFIntSubProjectBuff)buff );
			}
		}
		return( filteredList.toArray( new CFIntSubProjectBuff[0] ) );
	}

	public CFIntSubProjectBuff[] readBuffByTopProjectIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 TopProjectId )
	{
		final String S_ProcName = "CFIntRamSubProject.readBuffByTopProjectIdx() ";
		CFIntSubProjectBuff buff;
		ArrayList<CFIntSubProjectBuff> filteredList = new ArrayList<CFIntSubProjectBuff>();
		CFIntSubProjectBuff[] buffList = readDerivedByTopProjectIdx( Authorization,
			TopProjectId );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && buff.getClassCode().equals( "a105" ) ) {
				filteredList.add( (CFIntSubProjectBuff)buff );
			}
		}
		return( filteredList.toArray( new CFIntSubProjectBuff[0] ) );
	}

	public CFIntSubProjectBuff readBuffByNameIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 TopProjectId,
		String Name )
	{
		final String S_ProcName = "CFIntRamSubProject.readBuffByNameIdx() ";
		CFIntSubProjectBuff buff = readDerivedByNameIdx( Authorization,
			TopProjectId,
			Name );
		if( ( buff != null ) && buff.getClassCode().equals( "a105" ) ) {
			return( (CFIntSubProjectBuff)buff );
		}
		else {
			return( null );
		}
	}

	public void updateSubProject( CFSecAuthorization Authorization,
		CFIntSubProjectBuff Buff )
	{
		CFIntSubProjectPKey pkey = schema.getFactorySubProject().newPKey();
		pkey.setRequiredId( Buff.getRequiredId() );
		CFIntSubProjectBuff existing = dictByPKey.get( pkey );
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
		CFIntSubProjectByTenantIdxKey existingKeyTenantIdx = schema.getFactorySubProject().newTenantIdxKey();
		existingKeyTenantIdx.setRequiredTenantId( existing.getRequiredTenantId() );

		CFIntSubProjectByTenantIdxKey newKeyTenantIdx = schema.getFactorySubProject().newTenantIdxKey();
		newKeyTenantIdx.setRequiredTenantId( Buff.getRequiredTenantId() );

		CFIntSubProjectByTopProjectIdxKey existingKeyTopProjectIdx = schema.getFactorySubProject().newTopProjectIdxKey();
		existingKeyTopProjectIdx.setRequiredTopProjectId( existing.getRequiredTopProjectId() );

		CFIntSubProjectByTopProjectIdxKey newKeyTopProjectIdx = schema.getFactorySubProject().newTopProjectIdxKey();
		newKeyTopProjectIdx.setRequiredTopProjectId( Buff.getRequiredTopProjectId() );

		CFIntSubProjectByNameIdxKey existingKeyNameIdx = schema.getFactorySubProject().newNameIdxKey();
		existingKeyNameIdx.setRequiredTopProjectId( existing.getRequiredTopProjectId() );
		existingKeyNameIdx.setRequiredName( existing.getRequiredName() );

		CFIntSubProjectByNameIdxKey newKeyNameIdx = schema.getFactorySubProject().newNameIdxKey();
		newKeyNameIdx.setRequiredTopProjectId( Buff.getRequiredTopProjectId() );
		newKeyNameIdx.setRequiredName( Buff.getRequiredName() );

		// Check unique indexes

		if( ! existingKeyNameIdx.equals( newKeyNameIdx ) ) {
			if( dictByNameIdx.containsKey( newKeyNameIdx ) ) {
				throw new CFLibUniqueIndexViolationException( getClass(),
					"updateSubProject",
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

		Map< CFIntSubProjectPKey, CFIntSubProjectBuff > subdict;

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
			subdict = new HashMap< CFIntSubProjectPKey, CFIntSubProjectBuff >();
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
			subdict = new HashMap< CFIntSubProjectPKey, CFIntSubProjectBuff >();
			dictByTopProjectIdx.put( newKeyTopProjectIdx, subdict );
		}
		subdict.put( pkey, Buff );

		dictByNameIdx.remove( existingKeyNameIdx );
		dictByNameIdx.put( newKeyNameIdx, Buff );

	}

	public void deleteSubProject( CFSecAuthorization Authorization,
		CFIntSubProjectBuff Buff )
	{
		final String S_ProcName = "CFIntRamSubProjectTable.deleteSubProject() ";
		String classCode;
		CFIntSubProjectPKey pkey = schema.getFactorySubProject().newPKey();
		pkey.setRequiredId( Buff.getRequiredId() );
		CFIntSubProjectBuff existing = dictByPKey.get( pkey );
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
		CFIntSubProjectByTenantIdxKey keyTenantIdx = schema.getFactorySubProject().newTenantIdxKey();
		keyTenantIdx.setRequiredTenantId( existing.getRequiredTenantId() );

		CFIntSubProjectByTopProjectIdxKey keyTopProjectIdx = schema.getFactorySubProject().newTopProjectIdxKey();
		keyTopProjectIdx.setRequiredTopProjectId( existing.getRequiredTopProjectId() );

		CFIntSubProjectByNameIdxKey keyNameIdx = schema.getFactorySubProject().newNameIdxKey();
		keyNameIdx.setRequiredTopProjectId( existing.getRequiredTopProjectId() );
		keyNameIdx.setRequiredName( existing.getRequiredName() );

		// Validate reverse foreign keys

		// Delete is valid
		Map< CFIntSubProjectPKey, CFIntSubProjectBuff > subdict;

		dictByPKey.remove( pkey );

		subdict = dictByTenantIdx.get( keyTenantIdx );
		subdict.remove( pkey );

		subdict = dictByTopProjectIdx.get( keyTopProjectIdx );
		subdict.remove( pkey );

		dictByNameIdx.remove( keyNameIdx );

	}
	public void deleteSubProjectByIdIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 argId )
	{
		CFIntSubProjectPKey key = schema.getFactorySubProject().newPKey();
		key.setRequiredId( argId );
		deleteSubProjectByIdIdx( Authorization, key );
	}

	public void deleteSubProjectByIdIdx( CFSecAuthorization Authorization,
		CFIntSubProjectPKey argKey )
	{
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		CFIntSubProjectBuff cur;
		LinkedList<CFIntSubProjectBuff> matchSet = new LinkedList<CFIntSubProjectBuff>();
		Iterator<CFIntSubProjectBuff> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntSubProjectBuff> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = schema.getTableSubProject().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() );
			deleteSubProject( Authorization, cur );
		}
	}

	public void deleteSubProjectByTenantIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 argTenantId )
	{
		CFIntSubProjectByTenantIdxKey key = schema.getFactorySubProject().newTenantIdxKey();
		key.setRequiredTenantId( argTenantId );
		deleteSubProjectByTenantIdx( Authorization, key );
	}

	public void deleteSubProjectByTenantIdx( CFSecAuthorization Authorization,
		CFIntSubProjectByTenantIdxKey argKey )
	{
		CFIntSubProjectBuff cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<CFIntSubProjectBuff> matchSet = new LinkedList<CFIntSubProjectBuff>();
		Iterator<CFIntSubProjectBuff> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntSubProjectBuff> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = schema.getTableSubProject().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() );
			deleteSubProject( Authorization, cur );
		}
	}

	public void deleteSubProjectByTopProjectIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 argTopProjectId )
	{
		CFIntSubProjectByTopProjectIdxKey key = schema.getFactorySubProject().newTopProjectIdxKey();
		key.setRequiredTopProjectId( argTopProjectId );
		deleteSubProjectByTopProjectIdx( Authorization, key );
	}

	public void deleteSubProjectByTopProjectIdx( CFSecAuthorization Authorization,
		CFIntSubProjectByTopProjectIdxKey argKey )
	{
		CFIntSubProjectBuff cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<CFIntSubProjectBuff> matchSet = new LinkedList<CFIntSubProjectBuff>();
		Iterator<CFIntSubProjectBuff> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntSubProjectBuff> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = schema.getTableSubProject().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() );
			deleteSubProject( Authorization, cur );
		}
	}

	public void deleteSubProjectByNameIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 argTopProjectId,
		String argName )
	{
		CFIntSubProjectByNameIdxKey key = schema.getFactorySubProject().newNameIdxKey();
		key.setRequiredTopProjectId( argTopProjectId );
		key.setRequiredName( argName );
		deleteSubProjectByNameIdx( Authorization, key );
	}

	public void deleteSubProjectByNameIdx( CFSecAuthorization Authorization,
		CFIntSubProjectByNameIdxKey argKey )
	{
		CFIntSubProjectBuff cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<CFIntSubProjectBuff> matchSet = new LinkedList<CFIntSubProjectBuff>();
		Iterator<CFIntSubProjectBuff> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntSubProjectBuff> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = schema.getTableSubProject().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() );
			deleteSubProject( Authorization, cur );
		}
	}
}
