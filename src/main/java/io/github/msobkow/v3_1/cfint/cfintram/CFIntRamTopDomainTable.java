
// Description: Java 25 in-memory RAM DbIO implementation for TopDomain.

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
 *	CFIntRamTopDomainTable in-memory RAM DbIO implementation
 *	for TopDomain.
 */
public class CFIntRamTopDomainTable
	implements ICFIntTopDomainTable
{
	private ICFIntSchema schema;
	private Map< CFIntTopDomainPKey,
				CFIntTopDomainBuff > dictByPKey
		= new HashMap< CFIntTopDomainPKey,
				CFIntTopDomainBuff >();
	private Map< CFIntTopDomainByTenantIdxKey,
				Map< CFIntTopDomainPKey,
					CFIntTopDomainBuff >> dictByTenantIdx
		= new HashMap< CFIntTopDomainByTenantIdxKey,
				Map< CFIntTopDomainPKey,
					CFIntTopDomainBuff >>();
	private Map< CFIntTopDomainByTldIdxKey,
				Map< CFIntTopDomainPKey,
					CFIntTopDomainBuff >> dictByTldIdx
		= new HashMap< CFIntTopDomainByTldIdxKey,
				Map< CFIntTopDomainPKey,
					CFIntTopDomainBuff >>();
	private Map< CFIntTopDomainByNameIdxKey,
			CFIntTopDomainBuff > dictByNameIdx
		= new HashMap< CFIntTopDomainByNameIdxKey,
			CFIntTopDomainBuff >();

	public CFIntRamTopDomainTable( ICFIntSchema argSchema ) {
		schema = argSchema;
	}

	public void createTopDomain( CFSecAuthorization Authorization,
		CFIntTopDomainBuff Buff )
	{
		final String S_ProcName = "createTopDomain";
		CFIntTopDomainPKey pkey = schema.getFactoryTopDomain().newPKey();
		pkey.setRequiredId( schema.nextTopDomainIdGen() );
		Buff.setRequiredId( pkey.getRequiredId() );
		CFIntTopDomainByTenantIdxKey keyTenantIdx = schema.getFactoryTopDomain().newTenantIdxKey();
		keyTenantIdx.setRequiredTenantId( Buff.getRequiredTenantId() );

		CFIntTopDomainByTldIdxKey keyTldIdx = schema.getFactoryTopDomain().newTldIdxKey();
		keyTldIdx.setRequiredTldId( Buff.getRequiredTldId() );

		CFIntTopDomainByNameIdxKey keyNameIdx = schema.getFactoryTopDomain().newNameIdxKey();
		keyNameIdx.setRequiredTldId( Buff.getRequiredTldId() );
		keyNameIdx.setRequiredName( Buff.getRequiredName() );

		// Validate unique indexes

		if( dictByPKey.containsKey( pkey ) ) {
			throw new CFLibPrimaryKeyNotNewException( getClass(), S_ProcName, pkey );
		}

		if( dictByNameIdx.containsKey( keyNameIdx ) ) {
			throw new CFLibUniqueIndexViolationException( getClass(),
				S_ProcName,
				"TopDomNameIdx",
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
				if( null == schema.getTableTld().readDerivedByIdIdx( Authorization,
						Buff.getRequiredTldId() ) )
				{
					throw new CFLibUnresolvedRelationException( getClass(),
						S_ProcName,
						"Container",
						"ParentTld",
						"Tld",
						null );
				}
			}
		}

		// Proceed with adding the new record

		dictByPKey.put( pkey, Buff );

		Map< CFIntTopDomainPKey, CFIntTopDomainBuff > subdictTenantIdx;
		if( dictByTenantIdx.containsKey( keyTenantIdx ) ) {
			subdictTenantIdx = dictByTenantIdx.get( keyTenantIdx );
		}
		else {
			subdictTenantIdx = new HashMap< CFIntTopDomainPKey, CFIntTopDomainBuff >();
			dictByTenantIdx.put( keyTenantIdx, subdictTenantIdx );
		}
		subdictTenantIdx.put( pkey, Buff );

		Map< CFIntTopDomainPKey, CFIntTopDomainBuff > subdictTldIdx;
		if( dictByTldIdx.containsKey( keyTldIdx ) ) {
			subdictTldIdx = dictByTldIdx.get( keyTldIdx );
		}
		else {
			subdictTldIdx = new HashMap< CFIntTopDomainPKey, CFIntTopDomainBuff >();
			dictByTldIdx.put( keyTldIdx, subdictTldIdx );
		}
		subdictTldIdx.put( pkey, Buff );

		dictByNameIdx.put( keyNameIdx, Buff );

	}

	public CFIntTopDomainBuff readDerived( CFSecAuthorization Authorization,
		CFIntTopDomainPKey PKey )
	{
		final String S_ProcName = "CFIntRamTopDomain.readDerived";
		CFIntTopDomainPKey key = schema.getFactoryTopDomain().newPKey();
		key.setRequiredId( PKey.getRequiredId() );
		CFIntTopDomainBuff buff;
		if( dictByPKey.containsKey( key ) ) {
			buff = dictByPKey.get( key );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	public CFIntTopDomainBuff lockDerived( CFSecAuthorization Authorization,
		CFIntTopDomainPKey PKey )
	{
		final String S_ProcName = "CFIntRamTopDomain.readDerived";
		CFIntTopDomainPKey key = schema.getFactoryTopDomain().newPKey();
		key.setRequiredId( PKey.getRequiredId() );
		CFIntTopDomainBuff buff;
		if( dictByPKey.containsKey( key ) ) {
			buff = dictByPKey.get( key );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	public CFIntTopDomainBuff[] readAllDerived( CFSecAuthorization Authorization ) {
		final String S_ProcName = "CFIntRamTopDomain.readAllDerived";
		CFIntTopDomainBuff[] retList = new CFIntTopDomainBuff[ dictByPKey.values().size() ];
		Iterator< CFIntTopDomainBuff > iter = dictByPKey.values().iterator();
		int idx = 0;
		while( iter.hasNext() ) {
			retList[ idx++ ] = iter.next();
		}
		return( retList );
	}

	public CFIntTopDomainBuff[] readDerivedByTenantIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 TenantId )
	{
		final String S_ProcName = "CFIntRamTopDomain.readDerivedByTenantIdx";
		CFIntTopDomainByTenantIdxKey key = schema.getFactoryTopDomain().newTenantIdxKey();
		key.setRequiredTenantId( TenantId );

		CFIntTopDomainBuff[] recArray;
		if( dictByTenantIdx.containsKey( key ) ) {
			Map< CFIntTopDomainPKey, CFIntTopDomainBuff > subdictTenantIdx
				= dictByTenantIdx.get( key );
			recArray = new CFIntTopDomainBuff[ subdictTenantIdx.size() ];
			Iterator< CFIntTopDomainBuff > iter = subdictTenantIdx.values().iterator();
			int idx = 0;
			while( iter.hasNext() ) {
				recArray[ idx++ ] = iter.next();
			}
		}
		else {
			Map< CFIntTopDomainPKey, CFIntTopDomainBuff > subdictTenantIdx
				= new HashMap< CFIntTopDomainPKey, CFIntTopDomainBuff >();
			dictByTenantIdx.put( key, subdictTenantIdx );
			recArray = new CFIntTopDomainBuff[0];
		}
		return( recArray );
	}

	public CFIntTopDomainBuff[] readDerivedByTldIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 TldId )
	{
		final String S_ProcName = "CFIntRamTopDomain.readDerivedByTldIdx";
		CFIntTopDomainByTldIdxKey key = schema.getFactoryTopDomain().newTldIdxKey();
		key.setRequiredTldId( TldId );

		CFIntTopDomainBuff[] recArray;
		if( dictByTldIdx.containsKey( key ) ) {
			Map< CFIntTopDomainPKey, CFIntTopDomainBuff > subdictTldIdx
				= dictByTldIdx.get( key );
			recArray = new CFIntTopDomainBuff[ subdictTldIdx.size() ];
			Iterator< CFIntTopDomainBuff > iter = subdictTldIdx.values().iterator();
			int idx = 0;
			while( iter.hasNext() ) {
				recArray[ idx++ ] = iter.next();
			}
		}
		else {
			Map< CFIntTopDomainPKey, CFIntTopDomainBuff > subdictTldIdx
				= new HashMap< CFIntTopDomainPKey, CFIntTopDomainBuff >();
			dictByTldIdx.put( key, subdictTldIdx );
			recArray = new CFIntTopDomainBuff[0];
		}
		return( recArray );
	}

	public CFIntTopDomainBuff readDerivedByNameIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 TldId,
		String Name )
	{
		final String S_ProcName = "CFIntRamTopDomain.readDerivedByNameIdx";
		CFIntTopDomainByNameIdxKey key = schema.getFactoryTopDomain().newNameIdxKey();
		key.setRequiredTldId( TldId );
		key.setRequiredName( Name );

		CFIntTopDomainBuff buff;
		if( dictByNameIdx.containsKey( key ) ) {
			buff = dictByNameIdx.get( key );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	public CFIntTopDomainBuff readDerivedByIdIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 Id )
	{
		final String S_ProcName = "CFIntRamTopDomain.readDerivedByIdIdx() ";
		CFIntTopDomainPKey key = schema.getFactoryTopDomain().newPKey();
		key.setRequiredId( Id );

		CFIntTopDomainBuff buff;
		if( dictByPKey.containsKey( key ) ) {
			buff = dictByPKey.get( key );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	public CFIntTopDomainBuff readBuff( CFSecAuthorization Authorization,
		CFIntTopDomainPKey PKey )
	{
		final String S_ProcName = "CFIntRamTopDomain.readBuff";
		CFIntTopDomainBuff buff = readDerived( Authorization, PKey );
		if( ( buff != null ) && ( ! buff.getClassCode().equals( "a107" ) ) ) {
			buff = null;
		}
		return( buff );
	}

	public CFIntTopDomainBuff lockBuff( CFSecAuthorization Authorization,
		CFIntTopDomainPKey PKey )
	{
		final String S_ProcName = "lockBuff";
		CFIntTopDomainBuff buff = readDerived( Authorization, PKey );
		if( ( buff != null ) && ( ! buff.getClassCode().equals( "a107" ) ) ) {
			buff = null;
		}
		return( buff );
	}

	public CFIntTopDomainBuff[] readAllBuff( CFSecAuthorization Authorization )
	{
		final String S_ProcName = "CFIntRamTopDomain.readAllBuff";
		CFIntTopDomainBuff buff;
		ArrayList<CFIntTopDomainBuff> filteredList = new ArrayList<CFIntTopDomainBuff>();
		CFIntTopDomainBuff[] buffList = readAllDerived( Authorization );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && buff.getClassCode().equals( "a107" ) ) {
				filteredList.add( buff );
			}
		}
		return( filteredList.toArray( new CFIntTopDomainBuff[0] ) );
	}

	public CFIntTopDomainBuff readBuffByIdIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 Id )
	{
		final String S_ProcName = "CFIntRamTopDomain.readBuffByIdIdx() ";
		CFIntTopDomainBuff buff = readDerivedByIdIdx( Authorization,
			Id );
		if( ( buff != null ) && buff.getClassCode().equals( "a107" ) ) {
			return( (CFIntTopDomainBuff)buff );
		}
		else {
			return( null );
		}
	}

	public CFIntTopDomainBuff[] readBuffByTenantIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 TenantId )
	{
		final String S_ProcName = "CFIntRamTopDomain.readBuffByTenantIdx() ";
		CFIntTopDomainBuff buff;
		ArrayList<CFIntTopDomainBuff> filteredList = new ArrayList<CFIntTopDomainBuff>();
		CFIntTopDomainBuff[] buffList = readDerivedByTenantIdx( Authorization,
			TenantId );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && buff.getClassCode().equals( "a107" ) ) {
				filteredList.add( (CFIntTopDomainBuff)buff );
			}
		}
		return( filteredList.toArray( new CFIntTopDomainBuff[0] ) );
	}

	public CFIntTopDomainBuff[] readBuffByTldIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 TldId )
	{
		final String S_ProcName = "CFIntRamTopDomain.readBuffByTldIdx() ";
		CFIntTopDomainBuff buff;
		ArrayList<CFIntTopDomainBuff> filteredList = new ArrayList<CFIntTopDomainBuff>();
		CFIntTopDomainBuff[] buffList = readDerivedByTldIdx( Authorization,
			TldId );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && buff.getClassCode().equals( "a107" ) ) {
				filteredList.add( (CFIntTopDomainBuff)buff );
			}
		}
		return( filteredList.toArray( new CFIntTopDomainBuff[0] ) );
	}

	public CFIntTopDomainBuff readBuffByNameIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 TldId,
		String Name )
	{
		final String S_ProcName = "CFIntRamTopDomain.readBuffByNameIdx() ";
		CFIntTopDomainBuff buff = readDerivedByNameIdx( Authorization,
			TldId,
			Name );
		if( ( buff != null ) && buff.getClassCode().equals( "a107" ) ) {
			return( (CFIntTopDomainBuff)buff );
		}
		else {
			return( null );
		}
	}

	public void updateTopDomain( CFSecAuthorization Authorization,
		CFIntTopDomainBuff Buff )
	{
		CFIntTopDomainPKey pkey = schema.getFactoryTopDomain().newPKey();
		pkey.setRequiredId( Buff.getRequiredId() );
		CFIntTopDomainBuff existing = dictByPKey.get( pkey );
		if( existing == null ) {
			throw new CFLibStaleCacheDetectedException( getClass(),
				"updateTopDomain",
				"Existing record not found",
				"TopDomain",
				pkey );
		}
		if( existing.getRequiredRevision() != Buff.getRequiredRevision() ) {
			throw new CFLibCollisionDetectedException( getClass(),
				"updateTopDomain",
				pkey );
		}
		Buff.setRequiredRevision( Buff.getRequiredRevision() + 1 );
		CFIntTopDomainByTenantIdxKey existingKeyTenantIdx = schema.getFactoryTopDomain().newTenantIdxKey();
		existingKeyTenantIdx.setRequiredTenantId( existing.getRequiredTenantId() );

		CFIntTopDomainByTenantIdxKey newKeyTenantIdx = schema.getFactoryTopDomain().newTenantIdxKey();
		newKeyTenantIdx.setRequiredTenantId( Buff.getRequiredTenantId() );

		CFIntTopDomainByTldIdxKey existingKeyTldIdx = schema.getFactoryTopDomain().newTldIdxKey();
		existingKeyTldIdx.setRequiredTldId( existing.getRequiredTldId() );

		CFIntTopDomainByTldIdxKey newKeyTldIdx = schema.getFactoryTopDomain().newTldIdxKey();
		newKeyTldIdx.setRequiredTldId( Buff.getRequiredTldId() );

		CFIntTopDomainByNameIdxKey existingKeyNameIdx = schema.getFactoryTopDomain().newNameIdxKey();
		existingKeyNameIdx.setRequiredTldId( existing.getRequiredTldId() );
		existingKeyNameIdx.setRequiredName( existing.getRequiredName() );

		CFIntTopDomainByNameIdxKey newKeyNameIdx = schema.getFactoryTopDomain().newNameIdxKey();
		newKeyNameIdx.setRequiredTldId( Buff.getRequiredTldId() );
		newKeyNameIdx.setRequiredName( Buff.getRequiredName() );

		// Check unique indexes

		if( ! existingKeyNameIdx.equals( newKeyNameIdx ) ) {
			if( dictByNameIdx.containsKey( newKeyNameIdx ) ) {
				throw new CFLibUniqueIndexViolationException( getClass(),
					"updateTopDomain",
					"TopDomNameIdx",
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
						"updateTopDomain",
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
				if( null == schema.getTableTld().readDerivedByIdIdx( Authorization,
						Buff.getRequiredTldId() ) )
				{
					throw new CFLibUnresolvedRelationException( getClass(),
						"updateTopDomain",
						"Container",
						"ParentTld",
						"Tld",
						null );
				}
			}
		}

		// Update is valid

		Map< CFIntTopDomainPKey, CFIntTopDomainBuff > subdict;

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
			subdict = new HashMap< CFIntTopDomainPKey, CFIntTopDomainBuff >();
			dictByTenantIdx.put( newKeyTenantIdx, subdict );
		}
		subdict.put( pkey, Buff );

		subdict = dictByTldIdx.get( existingKeyTldIdx );
		if( subdict != null ) {
			subdict.remove( pkey );
		}
		if( dictByTldIdx.containsKey( newKeyTldIdx ) ) {
			subdict = dictByTldIdx.get( newKeyTldIdx );
		}
		else {
			subdict = new HashMap< CFIntTopDomainPKey, CFIntTopDomainBuff >();
			dictByTldIdx.put( newKeyTldIdx, subdict );
		}
		subdict.put( pkey, Buff );

		dictByNameIdx.remove( existingKeyNameIdx );
		dictByNameIdx.put( newKeyNameIdx, Buff );

	}

	public void deleteTopDomain( CFSecAuthorization Authorization,
		CFIntTopDomainBuff Buff )
	{
		final String S_ProcName = "CFIntRamTopDomainTable.deleteTopDomain() ";
		String classCode;
		CFIntTopDomainPKey pkey = schema.getFactoryTopDomain().newPKey();
		pkey.setRequiredId( Buff.getRequiredId() );
		CFIntTopDomainBuff existing = dictByPKey.get( pkey );
		if( existing == null ) {
			return;
		}
		if( existing.getRequiredRevision() != Buff.getRequiredRevision() )
		{
			throw new CFLibCollisionDetectedException( getClass(),
				"deleteTopDomain",
				pkey );
		}
					schema.getTableTopProject().deleteTopProjectByTopDomainIdx( Authorization,
						existing.getRequiredId() );
		CFIntTopDomainByTenantIdxKey keyTenantIdx = schema.getFactoryTopDomain().newTenantIdxKey();
		keyTenantIdx.setRequiredTenantId( existing.getRequiredTenantId() );

		CFIntTopDomainByTldIdxKey keyTldIdx = schema.getFactoryTopDomain().newTldIdxKey();
		keyTldIdx.setRequiredTldId( existing.getRequiredTldId() );

		CFIntTopDomainByNameIdxKey keyNameIdx = schema.getFactoryTopDomain().newNameIdxKey();
		keyNameIdx.setRequiredTldId( existing.getRequiredTldId() );
		keyNameIdx.setRequiredName( existing.getRequiredName() );

		// Validate reverse foreign keys

		// Delete is valid
		Map< CFIntTopDomainPKey, CFIntTopDomainBuff > subdict;

		dictByPKey.remove( pkey );

		subdict = dictByTenantIdx.get( keyTenantIdx );
		subdict.remove( pkey );

		subdict = dictByTldIdx.get( keyTldIdx );
		subdict.remove( pkey );

		dictByNameIdx.remove( keyNameIdx );

	}
	public void deleteTopDomainByIdIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 argId )
	{
		CFIntTopDomainPKey key = schema.getFactoryTopDomain().newPKey();
		key.setRequiredId( argId );
		deleteTopDomainByIdIdx( Authorization, key );
	}

	public void deleteTopDomainByIdIdx( CFSecAuthorization Authorization,
		CFIntTopDomainPKey argKey )
	{
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		CFIntTopDomainBuff cur;
		LinkedList<CFIntTopDomainBuff> matchSet = new LinkedList<CFIntTopDomainBuff>();
		Iterator<CFIntTopDomainBuff> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntTopDomainBuff> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = schema.getTableTopDomain().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() );
			deleteTopDomain( Authorization, cur );
		}
	}

	public void deleteTopDomainByTenantIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 argTenantId )
	{
		CFIntTopDomainByTenantIdxKey key = schema.getFactoryTopDomain().newTenantIdxKey();
		key.setRequiredTenantId( argTenantId );
		deleteTopDomainByTenantIdx( Authorization, key );
	}

	public void deleteTopDomainByTenantIdx( CFSecAuthorization Authorization,
		CFIntTopDomainByTenantIdxKey argKey )
	{
		CFIntTopDomainBuff cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<CFIntTopDomainBuff> matchSet = new LinkedList<CFIntTopDomainBuff>();
		Iterator<CFIntTopDomainBuff> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntTopDomainBuff> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = schema.getTableTopDomain().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() );
			deleteTopDomain( Authorization, cur );
		}
	}

	public void deleteTopDomainByTldIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 argTldId )
	{
		CFIntTopDomainByTldIdxKey key = schema.getFactoryTopDomain().newTldIdxKey();
		key.setRequiredTldId( argTldId );
		deleteTopDomainByTldIdx( Authorization, key );
	}

	public void deleteTopDomainByTldIdx( CFSecAuthorization Authorization,
		CFIntTopDomainByTldIdxKey argKey )
	{
		CFIntTopDomainBuff cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<CFIntTopDomainBuff> matchSet = new LinkedList<CFIntTopDomainBuff>();
		Iterator<CFIntTopDomainBuff> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntTopDomainBuff> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = schema.getTableTopDomain().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() );
			deleteTopDomain( Authorization, cur );
		}
	}

	public void deleteTopDomainByNameIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 argTldId,
		String argName )
	{
		CFIntTopDomainByNameIdxKey key = schema.getFactoryTopDomain().newNameIdxKey();
		key.setRequiredTldId( argTldId );
		key.setRequiredName( argName );
		deleteTopDomainByNameIdx( Authorization, key );
	}

	public void deleteTopDomainByNameIdx( CFSecAuthorization Authorization,
		CFIntTopDomainByNameIdxKey argKey )
	{
		CFIntTopDomainBuff cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<CFIntTopDomainBuff> matchSet = new LinkedList<CFIntTopDomainBuff>();
		Iterator<CFIntTopDomainBuff> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntTopDomainBuff> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = schema.getTableTopDomain().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() );
			deleteTopDomain( Authorization, cur );
		}
	}
}
