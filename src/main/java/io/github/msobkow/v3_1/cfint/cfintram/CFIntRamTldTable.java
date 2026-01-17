
// Description: Java 25 in-memory RAM DbIO implementation for Tld.

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
 *	CFIntRamTldTable in-memory RAM DbIO implementation
 *	for Tld.
 */
public class CFIntRamTldTable
	implements ICFIntTldTable
{
	private ICFIntSchema schema;
	private Map< CFIntTldPKey,
				CFIntTldBuff > dictByPKey
		= new HashMap< CFIntTldPKey,
				CFIntTldBuff >();
	private Map< CFIntTldByTenantIdxKey,
				Map< CFIntTldPKey,
					CFIntTldBuff >> dictByTenantIdx
		= new HashMap< CFIntTldByTenantIdxKey,
				Map< CFIntTldPKey,
					CFIntTldBuff >>();
	private Map< CFIntTldByNameIdxKey,
			CFIntTldBuff > dictByNameIdx
		= new HashMap< CFIntTldByNameIdxKey,
			CFIntTldBuff >();

	public CFIntRamTldTable( ICFIntSchema argSchema ) {
		schema = argSchema;
	}

	public void createTld( CFSecAuthorization Authorization,
		CFIntTldBuff Buff )
	{
		final String S_ProcName = "createTld";
		CFIntTldPKey pkey = schema.getFactoryTld().newPKey();
		pkey.setRequiredId( schema.nextTldIdGen() );
		Buff.setRequiredId( pkey.getRequiredId() );
		CFIntTldByTenantIdxKey keyTenantIdx = schema.getFactoryTld().newTenantIdxKey();
		keyTenantIdx.setRequiredTenantId( Buff.getRequiredTenantId() );

		CFIntTldByNameIdxKey keyNameIdx = schema.getFactoryTld().newNameIdxKey();
		keyNameIdx.setRequiredName( Buff.getRequiredName() );

		// Validate unique indexes

		if( dictByPKey.containsKey( pkey ) ) {
			throw new CFLibPrimaryKeyNotNewException( getClass(), S_ProcName, pkey );
		}

		if( dictByNameIdx.containsKey( keyNameIdx ) ) {
			throw new CFLibUniqueIndexViolationException( getClass(),
				S_ProcName,
				"TldNameIdx",
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
						"Container",
						"TldTenant",
						"Tenant",
						null );
				}
			}
		}

		// Proceed with adding the new record

		dictByPKey.put( pkey, Buff );

		Map< CFIntTldPKey, CFIntTldBuff > subdictTenantIdx;
		if( dictByTenantIdx.containsKey( keyTenantIdx ) ) {
			subdictTenantIdx = dictByTenantIdx.get( keyTenantIdx );
		}
		else {
			subdictTenantIdx = new HashMap< CFIntTldPKey, CFIntTldBuff >();
			dictByTenantIdx.put( keyTenantIdx, subdictTenantIdx );
		}
		subdictTenantIdx.put( pkey, Buff );

		dictByNameIdx.put( keyNameIdx, Buff );

	}

	public CFIntTldBuff readDerived( CFSecAuthorization Authorization,
		CFIntTldPKey PKey )
	{
		final String S_ProcName = "CFIntRamTld.readDerived";
		CFIntTldPKey key = schema.getFactoryTld().newPKey();
		key.setRequiredId( PKey.getRequiredId() );
		CFIntTldBuff buff;
		if( dictByPKey.containsKey( key ) ) {
			buff = dictByPKey.get( key );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	public CFIntTldBuff lockDerived( CFSecAuthorization Authorization,
		CFIntTldPKey PKey )
	{
		final String S_ProcName = "CFIntRamTld.readDerived";
		CFIntTldPKey key = schema.getFactoryTld().newPKey();
		key.setRequiredId( PKey.getRequiredId() );
		CFIntTldBuff buff;
		if( dictByPKey.containsKey( key ) ) {
			buff = dictByPKey.get( key );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	public CFIntTldBuff[] readAllDerived( CFSecAuthorization Authorization ) {
		final String S_ProcName = "CFIntRamTld.readAllDerived";
		CFIntTldBuff[] retList = new CFIntTldBuff[ dictByPKey.values().size() ];
		Iterator< CFIntTldBuff > iter = dictByPKey.values().iterator();
		int idx = 0;
		while( iter.hasNext() ) {
			retList[ idx++ ] = iter.next();
		}
		return( retList );
	}

	public CFIntTldBuff[] readDerivedByTenantIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 TenantId )
	{
		final String S_ProcName = "CFIntRamTld.readDerivedByTenantIdx";
		CFIntTldByTenantIdxKey key = schema.getFactoryTld().newTenantIdxKey();
		key.setRequiredTenantId( TenantId );

		CFIntTldBuff[] recArray;
		if( dictByTenantIdx.containsKey( key ) ) {
			Map< CFIntTldPKey, CFIntTldBuff > subdictTenantIdx
				= dictByTenantIdx.get( key );
			recArray = new CFIntTldBuff[ subdictTenantIdx.size() ];
			Iterator< CFIntTldBuff > iter = subdictTenantIdx.values().iterator();
			int idx = 0;
			while( iter.hasNext() ) {
				recArray[ idx++ ] = iter.next();
			}
		}
		else {
			Map< CFIntTldPKey, CFIntTldBuff > subdictTenantIdx
				= new HashMap< CFIntTldPKey, CFIntTldBuff >();
			dictByTenantIdx.put( key, subdictTenantIdx );
			recArray = new CFIntTldBuff[0];
		}
		return( recArray );
	}

	public CFIntTldBuff readDerivedByNameIdx( CFSecAuthorization Authorization,
		String Name )
	{
		final String S_ProcName = "CFIntRamTld.readDerivedByNameIdx";
		CFIntTldByNameIdxKey key = schema.getFactoryTld().newNameIdxKey();
		key.setRequiredName( Name );

		CFIntTldBuff buff;
		if( dictByNameIdx.containsKey( key ) ) {
			buff = dictByNameIdx.get( key );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	public CFIntTldBuff readDerivedByIdIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 Id )
	{
		final String S_ProcName = "CFIntRamTld.readDerivedByIdIdx() ";
		CFIntTldPKey key = schema.getFactoryTld().newPKey();
		key.setRequiredId( Id );

		CFIntTldBuff buff;
		if( dictByPKey.containsKey( key ) ) {
			buff = dictByPKey.get( key );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	public CFIntTldBuff readBuff( CFSecAuthorization Authorization,
		CFIntTldPKey PKey )
	{
		final String S_ProcName = "CFIntRamTld.readBuff";
		CFIntTldBuff buff = readDerived( Authorization, PKey );
		if( ( buff != null ) && ( ! buff.getClassCode().equals( "a106" ) ) ) {
			buff = null;
		}
		return( buff );
	}

	public CFIntTldBuff lockBuff( CFSecAuthorization Authorization,
		CFIntTldPKey PKey )
	{
		final String S_ProcName = "lockBuff";
		CFIntTldBuff buff = readDerived( Authorization, PKey );
		if( ( buff != null ) && ( ! buff.getClassCode().equals( "a106" ) ) ) {
			buff = null;
		}
		return( buff );
	}

	public CFIntTldBuff[] readAllBuff( CFSecAuthorization Authorization )
	{
		final String S_ProcName = "CFIntRamTld.readAllBuff";
		CFIntTldBuff buff;
		ArrayList<CFIntTldBuff> filteredList = new ArrayList<CFIntTldBuff>();
		CFIntTldBuff[] buffList = readAllDerived( Authorization );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && buff.getClassCode().equals( "a106" ) ) {
				filteredList.add( buff );
			}
		}
		return( filteredList.toArray( new CFIntTldBuff[0] ) );
	}

	public CFIntTldBuff readBuffByIdIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 Id )
	{
		final String S_ProcName = "CFIntRamTld.readBuffByIdIdx() ";
		CFIntTldBuff buff = readDerivedByIdIdx( Authorization,
			Id );
		if( ( buff != null ) && buff.getClassCode().equals( "a106" ) ) {
			return( (CFIntTldBuff)buff );
		}
		else {
			return( null );
		}
	}

	public CFIntTldBuff[] readBuffByTenantIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 TenantId )
	{
		final String S_ProcName = "CFIntRamTld.readBuffByTenantIdx() ";
		CFIntTldBuff buff;
		ArrayList<CFIntTldBuff> filteredList = new ArrayList<CFIntTldBuff>();
		CFIntTldBuff[] buffList = readDerivedByTenantIdx( Authorization,
			TenantId );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && buff.getClassCode().equals( "a106" ) ) {
				filteredList.add( (CFIntTldBuff)buff );
			}
		}
		return( filteredList.toArray( new CFIntTldBuff[0] ) );
	}

	public CFIntTldBuff readBuffByNameIdx( CFSecAuthorization Authorization,
		String Name )
	{
		final String S_ProcName = "CFIntRamTld.readBuffByNameIdx() ";
		CFIntTldBuff buff = readDerivedByNameIdx( Authorization,
			Name );
		if( ( buff != null ) && buff.getClassCode().equals( "a106" ) ) {
			return( (CFIntTldBuff)buff );
		}
		else {
			return( null );
		}
	}

	public void updateTld( CFSecAuthorization Authorization,
		CFIntTldBuff Buff )
	{
		CFIntTldPKey pkey = schema.getFactoryTld().newPKey();
		pkey.setRequiredId( Buff.getRequiredId() );
		CFIntTldBuff existing = dictByPKey.get( pkey );
		if( existing == null ) {
			throw new CFLibStaleCacheDetectedException( getClass(),
				"updateTld",
				"Existing record not found",
				"Tld",
				pkey );
		}
		if( existing.getRequiredRevision() != Buff.getRequiredRevision() ) {
			throw new CFLibCollisionDetectedException( getClass(),
				"updateTld",
				pkey );
		}
		Buff.setRequiredRevision( Buff.getRequiredRevision() + 1 );
		CFIntTldByTenantIdxKey existingKeyTenantIdx = schema.getFactoryTld().newTenantIdxKey();
		existingKeyTenantIdx.setRequiredTenantId( existing.getRequiredTenantId() );

		CFIntTldByTenantIdxKey newKeyTenantIdx = schema.getFactoryTld().newTenantIdxKey();
		newKeyTenantIdx.setRequiredTenantId( Buff.getRequiredTenantId() );

		CFIntTldByNameIdxKey existingKeyNameIdx = schema.getFactoryTld().newNameIdxKey();
		existingKeyNameIdx.setRequiredName( existing.getRequiredName() );

		CFIntTldByNameIdxKey newKeyNameIdx = schema.getFactoryTld().newNameIdxKey();
		newKeyNameIdx.setRequiredName( Buff.getRequiredName() );

		// Check unique indexes

		if( ! existingKeyNameIdx.equals( newKeyNameIdx ) ) {
			if( dictByNameIdx.containsKey( newKeyNameIdx ) ) {
				throw new CFLibUniqueIndexViolationException( getClass(),
					"updateTld",
					"TldNameIdx",
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
						"updateTld",
						"Container",
						"TldTenant",
						"Tenant",
						null );
				}
			}
		}

		// Update is valid

		Map< CFIntTldPKey, CFIntTldBuff > subdict;

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
			subdict = new HashMap< CFIntTldPKey, CFIntTldBuff >();
			dictByTenantIdx.put( newKeyTenantIdx, subdict );
		}
		subdict.put( pkey, Buff );

		dictByNameIdx.remove( existingKeyNameIdx );
		dictByNameIdx.put( newKeyNameIdx, Buff );

	}

	public void deleteTld( CFSecAuthorization Authorization,
		CFIntTldBuff Buff )
	{
		final String S_ProcName = "CFIntRamTldTable.deleteTld() ";
		String classCode;
		CFIntTldPKey pkey = schema.getFactoryTld().newPKey();
		pkey.setRequiredId( Buff.getRequiredId() );
		CFIntTldBuff existing = dictByPKey.get( pkey );
		if( existing == null ) {
			return;
		}
		if( existing.getRequiredRevision() != Buff.getRequiredRevision() )
		{
			throw new CFLibCollisionDetectedException( getClass(),
				"deleteTld",
				pkey );
		}
					schema.getTableTopDomain().deleteTopDomainByTldIdx( Authorization,
						existing.getRequiredId() );
		CFIntTldByTenantIdxKey keyTenantIdx = schema.getFactoryTld().newTenantIdxKey();
		keyTenantIdx.setRequiredTenantId( existing.getRequiredTenantId() );

		CFIntTldByNameIdxKey keyNameIdx = schema.getFactoryTld().newNameIdxKey();
		keyNameIdx.setRequiredName( existing.getRequiredName() );

		// Validate reverse foreign keys

		// Delete is valid
		Map< CFIntTldPKey, CFIntTldBuff > subdict;

		dictByPKey.remove( pkey );

		subdict = dictByTenantIdx.get( keyTenantIdx );
		subdict.remove( pkey );

		dictByNameIdx.remove( keyNameIdx );

	}
	public void deleteTldByIdIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 argId )
	{
		CFIntTldPKey key = schema.getFactoryTld().newPKey();
		key.setRequiredId( argId );
		deleteTldByIdIdx( Authorization, key );
	}

	public void deleteTldByIdIdx( CFSecAuthorization Authorization,
		CFIntTldPKey argKey )
	{
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		CFIntTldBuff cur;
		LinkedList<CFIntTldBuff> matchSet = new LinkedList<CFIntTldBuff>();
		Iterator<CFIntTldBuff> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntTldBuff> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = schema.getTableTld().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() );
			deleteTld( Authorization, cur );
		}
	}

	public void deleteTldByTenantIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 argTenantId )
	{
		CFIntTldByTenantIdxKey key = schema.getFactoryTld().newTenantIdxKey();
		key.setRequiredTenantId( argTenantId );
		deleteTldByTenantIdx( Authorization, key );
	}

	public void deleteTldByTenantIdx( CFSecAuthorization Authorization,
		CFIntTldByTenantIdxKey argKey )
	{
		CFIntTldBuff cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<CFIntTldBuff> matchSet = new LinkedList<CFIntTldBuff>();
		Iterator<CFIntTldBuff> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntTldBuff> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = schema.getTableTld().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() );
			deleteTld( Authorization, cur );
		}
	}

	public void deleteTldByNameIdx( CFSecAuthorization Authorization,
		String argName )
	{
		CFIntTldByNameIdxKey key = schema.getFactoryTld().newNameIdxKey();
		key.setRequiredName( argName );
		deleteTldByNameIdx( Authorization, key );
	}

	public void deleteTldByNameIdx( CFSecAuthorization Authorization,
		CFIntTldByNameIdxKey argKey )
	{
		CFIntTldBuff cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<CFIntTldBuff> matchSet = new LinkedList<CFIntTldBuff>();
		Iterator<CFIntTldBuff> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntTldBuff> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = schema.getTableTld().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() );
			deleteTld( Authorization, cur );
		}
	}
}
